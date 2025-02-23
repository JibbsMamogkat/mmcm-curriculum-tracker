package com.mamogkat.mmcmcurriculumtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
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
}
