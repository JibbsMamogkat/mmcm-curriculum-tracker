package com.mamogkat.mmcmcurriculumtracker.viewmodel

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

                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val role = document.getString("role")

                                when (role) {
                                    "Student" -> navController.navigate("choose_curriculum")
                                    "Admin" -> navController.navigate("admin_home_page")
                                    null -> _errorMessage.value = "Account role is missing. Contact support."
                                    else -> _errorMessage.value = "Unknown role: $role"
                                }
                            } else {
                                _errorMessage.value = "Account not found. Please register first."
                            }
                            _isLoading.value = false
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
}