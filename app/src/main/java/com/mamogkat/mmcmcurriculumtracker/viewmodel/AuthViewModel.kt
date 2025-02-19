package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue

class AuthViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess

    fun registerUser(email: String, password: String, role: String, program: String, navController: NavController) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.createUserWithEmailAndPassword(
            email,
            password
        )
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
                                val adminDate = hashMapOf(
                                    "userID" to userId,
                                    "email" to email,
                                    "name" to "",
                                    "permission" to "admin"
                                )

                                db.collection("admins").document(userId)
                                    .set(adminDate)
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
}