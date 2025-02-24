package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _approvalStatus = MutableStateFlow<String?>(null) // Null until loaded
    val approvalStatus = _approvalStatus.asStateFlow()

    fun fetchApprovalStatus(studentId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("students").document(studentId).get().await()
                _approvalStatus.value = document.getString("approvalStatus")
            } catch (e: Exception) {
                e.printStackTrace()
                _approvalStatus.value = "error" // Handle gracefully if needed
            }
        }
    }
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun refreshData(studentId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchApprovalStatus(studentId)  // Re-fetch approval status
            _isRefreshing.value = false
        }
    }
    fun observeCurrentUserApprovalStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("students")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("approvalStatus") ?: "pending"
                    _approvalStatus.value = status // Auto-update Compose
                }
            }
    }

}
