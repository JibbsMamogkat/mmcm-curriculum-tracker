package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AuthViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess

    fun loginUser(email: String, password: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        // Check if the email exists in the "users" collection
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Email found, attempt to authenticate
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                Log.d("AuthViewModel", "User ID: $userId")

                                db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener { userDoc ->
                                        if (userDoc.exists()) {
                                            val role = userDoc.getString("role") ?: "Unknown"

                                            when (role) {
                                                "Student" -> {
                                                    db.collection("students").document(userId)
                                                        .get()
                                                        .addOnSuccessListener { studentDoc ->
                                                            if (studentDoc.exists()) {
                                                                val curriculum = studentDoc.getString("curriculum")

                                                                if (curriculum.isNullOrEmpty()) {
                                                                    navController.navigate("choose_curriculum") {
                                                                        popUpTo(0) { inclusive = true }
                                                                        launchSingleTop = true
                                                                    }
                                                                } else {
                                                                    navController.navigate("student_main") {
                                                                        popUpTo(0) { inclusive = true }
                                                                        launchSingleTop = true
                                                                    }
                                                                }
                                                            } else {
                                                                _errorMessage.value = "Student record not found. Contact support."
                                                            }
                                                            _isLoading.value = false
                                                        }
                                                        .addOnFailureListener { e ->
                                                            _errorMessage.value = "Error retrieving student data: ${e.message}"
                                                            _isLoading.value = false
                                                        }
                                                }

                                                "Admin" -> {
                                                    navController.navigate("admin_home_page") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                    _isLoading.value = false
                                                }

                                                else -> {
                                                    _errorMessage.value = "Unknown role: $role. Contact support."
                                                    _isLoading.value = false
                                                }
                                            }
                                        } else {
                                            _errorMessage.value = "Account not found. Please register first."
                                            _isLoading.value = false
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        _errorMessage.value = "Error retrieving user data: ${e.message}"
                                        _isLoading.value = false
                                    }
                            } else {
                                // Handle specific FirebaseAuth exceptions
                                val exception = task.exception
                                _errorMessage.value = when (exception) {
                                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                                    is FirebaseAuthInvalidUserException -> "User account not found. Please register."
                                    else -> "Login failed: ${exception?.message}"
                                }
                                _isLoading.value = false
                            }
                        }
                } else {
                    // Email not found in Firestore
                    _errorMessage.value = "Email is not registered."
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error checking email: ${e.message}"
                _isLoading.value = false
            }
    }


    fun setErrorMessage(s: String) {
        _errorMessage.value = s
    }

    // duff added feb -15
    fun checkUserCurriculum(onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("students").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val curriculum = document.getString("curriculum")
                onResult(!curriculum.isNullOrEmpty()) // Returns true if curriculum is set
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
    // ----------------------------
    // duff added feb 19
    fun registerUser(email: String, password: String, role: String, program: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    _errorMessage.value = "Email already registered."
                    _isLoading.value = false
                } else {
                    navController.navigate("loadingOTP")
                    // ✅ Timeout logic added here
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = withTimeoutOrNull(120_000) {  // Timeout after 2 minutes
                            suspendCoroutine { continuation ->
                                sendOtp(email) { success ->
                                    continuation.resume(success)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            _isLoading.value = false
                            if (result == true) {
                                navController.navigate("verify_otp/$email/$password/$role/$program") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                _errorMessage.value = if (result == null) {
                                    "Request timed out. Please try again. If the issue persists, contact Jameel or Duff."
                                } else {
                                    "Failed to send OTP. Please try again. If the issue persists, contact Jameel or Duff. Note: OTP requests may take longer during periods of high demand."
                                }
                                navController.navigate("error")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error checking email: ${e.message}"
                _isLoading.value = false
            }
    }
    fun sendOtp(email: String, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        val otp = (100000..999999).random().toString()
        Log.d("AuthViewModel", "Payload data: email=$email, otp=$otp")

        db.collection("otps").document(email)
            .set(
                hashMapOf(
                    "otp" to otp,
                    "expiresAt" to Timestamp(Date(System.currentTimeMillis() + 5 * 60 * 1000))
                )
            )
            .addOnSuccessListener {
                Log.d("AuthViewModel", "OTP saved in Firestore: $otp")

                // Build JSON payload
                val json = JSONObject().apply {
                    put("email", email)
                    put("otp", otp)
                }
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://us-central1-mmcm-curriculum-tracker-app.cloudfunctions.net/sendOtpEmail")
                    .post(requestBody)
                    .build()

                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("AuthViewModel", "Failed to send OTP email: ${e.message}", e)
                        _errorMessage.value = "Failed to send OTP email: ${e.message}"
                        _isLoading.value = false
                        onResult(false)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val bodyString = response.body?.string()
                            Log.d("AuthViewModel", "OTP email sent successfully: $bodyString")
                            onResult(true)
                        } else {
                            Log.e("AuthViewModel", "Failed to send OTP email: HTTP ${response.code}")
                            _errorMessage.value = "Failed to send OTP email: HTTP ${response.code}"
                            onResult(false)
                        }
                        _isLoading.value = false
                    }
                })
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to save OTP: ${e.message}"
                _isLoading.value = false
            }
    }
    fun verifyOtp(email: String, enteredOtp: String, password: String, role: String, program: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("otps").document(email)
            .get()
            .addOnSuccessListener { document ->
                val storedOtp = document.getString("otp")
                val expiresAt = document.getTimestamp("expiresAt")

                if (storedOtp == enteredOtp && expiresAt != null && expiresAt.toDate().after(Date())) {
                    // OTP is valid, proceed with registration
                    registerNewUser(email, password, role, program, navController)
                } else {
                    _errorMessage.value = "Invalid or expired OTP."
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error verifying OTP: ${e.message}"
                _isLoading.value = false
            }
    }
    private fun registerNewUser(email: String, password: String, role: String, program: String, navController: NavController) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = hashMapOf(
                        "email" to email,
                        "role" to role,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            if (role == "Student") {
                                val studentData = hashMapOf(
                                    "userID" to userId,
                                    "email" to email,
                                    "approvalStatus" to "pending",
                                    "name" to "",
                                    "studentNumber" to "",
                                    "program" to program,
                                    "curriculum" to "",
                                    "completedCourses" to emptyList<String>(),
                                    "termEnrolling" to 1,
                                )

                                db.collection("students").document(userId)
                                    .set(studentData)
                                    .addOnSuccessListener {
                                        _isSuccess.value = true
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                            } else {
                                val adminData = hashMapOf(
                                    "userID" to userId,
                                    "email" to email,
                                    "name" to "",
                                    "permission" to "admin"
                                )

                                db.collection("admins").document(userId)
                                    .set(adminData)
                                    .addOnSuccessListener {
                                        _isSuccess.value = true
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error saving user data: ${e.message}"
                        }
                } else {
                    _errorMessage.value = "Registration failed: ${task.exception?.message}"
                }
                _isLoading.value = false
            }
    }

    fun clearState() {
        _isLoading.value = false
        _errorMessage.value = null
        _isSuccess.value = false
    }
    private val _isSplash = mutableStateOf(true)  // New variable for the splash state
    val isSplash: State<Boolean> = _isSplash


    private val _isUserChecked = mutableStateOf(false)
    val isUserChecked: State<Boolean> get() = _isUserChecked

    fun checkUserLogin(navController: NavController) {
        if (_isUserChecked.value) return  // ✅ Avoid rechecking after rotation

        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val role = userDoc.getString("role") ?: "Unknown"
                        when (role) {
                            "Student" -> {
                                db.collection("students").document(userId).get()
                                    .addOnSuccessListener { studentDoc ->
                                        val destination = if (studentDoc.exists() && !studentDoc.getString("curriculum").isNullOrEmpty()) {
                                            "student_main"
                                        } else {
                                            "choose_curriculum"
                                        }
                                        navController.navigate(destination) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        _isUserChecked.value = true  // ✅ Set after navigation
                                        _isSplash.value = false
                                    }
                            }

                            "Admin" -> {
                                navController.navigate("admin_home_page") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                _isUserChecked.value = true
                                _isSplash.value = false
                            }

                            else -> {
                                _errorMessage.value = "Unknown role: $role. Contact support."
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                                _isUserChecked.value = true
                                _isSplash.value = false
                            }
                        }
                    } else {
                        _errorMessage.value = "Account not found. Please register first."
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                        _isUserChecked.value = true
                        _isSplash.value = false
                    }
                }
                .addOnFailureListener {
                    _errorMessage.value = "No internet connection or Firestore error."
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                    _isUserChecked.value = true
                    _isSplash.value = false
                }
        } else {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            _isUserChecked.value = true
            _isSplash.value = false
        }
    }
    private val _studentName = MutableStateFlow<String?>(null) // 👈 Null means loading state
    val studentName: StateFlow<String?> = _studentName.asStateFlow()

    init {
        fetchStudentName()
    }

    private fun fetchStudentName() {
        val currentUser = Firebase.auth.currentUser
        val studentId = currentUser?.uid ?: return

        Firebase.firestore.collection("students")
            .document(studentId)
            .get()
            .addOnSuccessListener { document ->
                _studentName.value = document.getString("name") ?: "No Name"
                _lastNameChangeTime.value = document.getTimestamp("lastNameChange")?.toDate()?.time
            }
            .addOnFailureListener {
                Log.e("AuthViewModel", "Failed to fetch name")
            }
    }


    fun logoutUser(navController: NavController) {
        auth.signOut() // Firebase sign out
        navController.navigate("login") {
            popUpTo(0) { inclusive = true } // Clear back stack so user can't go back
        }
    }
    // for FORGOT PASSWORD
    fun forgotPassword(email: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    navController.navigate("loadingOTP")
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = withTimeoutOrNull(120_000) {
                            suspendCoroutine { continuation ->
                                sendOtp(email) { success ->
                                    continuation.resume(success)
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            _isLoading.value = false
                            if (result == true) {
                                navController.navigate("verify_forgot_password_otp/$email") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                _errorMessage.value = if (result == null) {
                                    "Request timed out. Please try again."
                                } else {
                                    "Failed to send OTP. Please try again."
                                }
                                navController.navigate("error")
                            }
                        }
                    }
                } else {
                    _errorMessage.value = "Email not found. Please register first."
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error checking email: ${e.message}"
                _isLoading.value = false
            }
    }

    fun verifyForgotPasswordOtp(email: String, enteredOtp: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        db.collection("otps").document(email)
            .get()
            .addOnSuccessListener { document ->
                val storedOtp = document.getString("otp")
                val expiresAt = document.getTimestamp("expiresAt")

                if (storedOtp == enteredOtp && expiresAt != null && expiresAt.toDate().after(Date())) {
                    navController.navigate("change_password/$email") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    _errorMessage.value = "Invalid or expired OTP."
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error verifying OTP: ${e.message}"
                _isLoading.value = false
            }
    }
    fun changePassword(email: String, newPassword: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        val json = JSONObject().apply {
            put("email", email)
            put("newPassword", newPassword)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://us-central1-mmcm-curriculum-tracker-app.cloudfunctions.net/updateUserPassword")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _isLoading.value = false
                _errorMessage.value = "Error updating password: ${e.message}"
            }

            override fun onResponse(call: Call, response: Response) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    Handler(Looper.getMainLooper()).post {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    _errorMessage.value = "Error updating password: HTTP ${response.code}"
                }
            }
        })
    }
    private val _lastNameChangeTime = MutableStateFlow<Long?>(null) // Store last name change time
    val lastNameChangeTime: StateFlow<Long?> = _lastNameChangeTime


    fun updateStudentName(newName: String) {
        val currentUser = Firebase.auth.currentUser
        val studentId = currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val currentTime = Timestamp.now() // Current time
                val studentRef = Firebase.firestore.collection("students").document(studentId)

                studentRef.update(
                    mapOf(
                        "name" to newName,
                        "lastNameChange" to currentTime // Store last name change time
                    )
                ).await()

                _studentName.value = newName
                _lastNameChangeTime.value = currentTime.toDate().time // Convert Firestore Timestamp to Long
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update name: ${e.message}")
            }
        }
    }


}
