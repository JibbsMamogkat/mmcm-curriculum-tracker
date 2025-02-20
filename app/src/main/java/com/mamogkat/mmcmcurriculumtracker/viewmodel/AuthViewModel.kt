package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
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

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    Log.d("AuthViewModel", "User ID: $userId")


                    // First, get user role from "users" collection
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc != null && userDoc.exists()) {
                                val role = userDoc.getString("role") ?: "Unknown"

                                when (role) {
                                    "Student" -> {
                                        // Now fetch curriculum info from "students" collection
                                        db.collection("students").document(userId)
                                            .get()
                                            .addOnSuccessListener { studentDoc ->
                                                if (studentDoc != null && studentDoc.exists()) {
                                                    val curriculum = studentDoc.getString("curriculum")

                                                    if (curriculum.isNullOrEmpty()) {
                                                        navController.navigate("choose_curriculum")
                                                    } else {
                                                        navController.navigate("student_main")
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
                                        navController.navigate("admin_home_page")
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
                    _errorMessage.value = "Login failed: ${task.exception?.message}"
                    _isLoading.value = false
                }
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

        // Check if email already exists
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    _errorMessage.value = "Email already registered."
                    _isLoading.value = false
                } else {
                    // Email does not exist, send OTP and navigate to OTP verification screen
                    sendOtp(email) // Calls function to send OTP
                    navController.navigate("verify_otp/$email/$password/$role/$program") // Pass data to OTP screen
                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error checking email: ${e.message}"
                _isLoading.value = false
            }
    }

    fun sendOtp(email: String) {
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
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val bodyString = response.body?.string()
                            Log.d("AuthViewModel", "OTP email sent successfully: $bodyString")
                        } else {
                            Log.e("AuthViewModel", "Failed to send OTP email: HTTP ${response.code}")
                            _errorMessage.value = "Failed to send OTP email: HTTP ${response.code}"
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
                                    "name" to "",
                                    "studentNumber" to "",
                                    "program" to program,
                                    "curriculum" to "",
                                    "coursesCompleted" to emptyList<String>(),
                                    "termEnrolling" to "1",
                                )

                                db.collection("students").document(userId)
                                    .set(studentData)
                                    .addOnSuccessListener {
                                        _isSuccess.value = true
                                        navController.navigate("login")
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
                                        navController.navigate("login")
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
}