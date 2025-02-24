package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.*
import com.mamogkat.mmcmcurriculumtracker.models.Curriculum
import com.mamogkat.mmcmcurriculumtracker.models.Student
import com.mamogkat.mmcmcurriculumtracker.repository.FirebaseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _studentList = MutableLiveData<List<Student>>()
    val studentList: LiveData<List<Student>> = _studentList

    private val _curriculumList = MutableLiveData<List<Curriculum>>()
    val curriculumList: LiveData<List<Curriculum>> = _curriculumList

    private val _curriculumViewedMap = mutableStateMapOf<String, Boolean>() // Tracks if a student's curriculum was viewed
    val curriculumViewedMap: Map<String, Boolean> get() = _curriculumViewedMap

    private val _studentApprovalStatus = MutableLiveData<String>()
    val studentApprovalStatus: LiveData<String> get() = _studentApprovalStatus

    private val _studentCurriculum = MutableLiveData<String>()
    val studentCurriculum: LiveData<String> get() = _studentCurriculum

    private val _studentProgram = MutableLiveData<String>()
    val studentProgram: LiveData<String> get() = _studentProgram


    fun fetchStudents() {
        repository.getAllStudents { students ->
            _studentList.value = students
        }
    }

    fun fetchCurriculums() {
        repository.getAllCurriculums { curriculums ->
            _curriculumList.value = curriculums
        }
    }

    fun updateStudentCurriculum(studentId: String?, newCurriculumId: String) {
        if (studentId.isNullOrEmpty()) {
            Log.e("AdminViewModel", "Error: studentId is null or empty, cannot update curriculum")
            return
        }

        repository.updateStudentCurriculum(studentId, newCurriculumId) { success ->
            if (success) {
                Log.d("AdminViewModel", "Successfully updated curriculum for student: $studentId")
                fetchStudents() //refresh the student list to trigger UI recomposition
            } else {
                Log.e("AdminViewModel", "Failed to update curriculum for student: $studentId")
            }
        }
    }

    fun getCurriculumNameMap(): Map<String, String> {
        return curriculumList.value?.associate { it.curriculumID to it.name } ?: emptyMap()
    }

    fun removeStudent(studentId: String) {
        repository.removeStudent(studentId)
        fetchStudents()  // Refresh the student list
    }

    fun fetchStudentApprovalStatus(studentId: String) {
        repository.getStudentApprovalStatus(
            studentId,
            onComplete = { status ->
                _studentApprovalStatus.postValue(status)  // 🔥 Use postValue to ensure UI update
                Log.d("AdminViewModel", "Approval status for student $studentId: $status")
            },
            onError = { e ->
                Log.e("AdminViewModel", "Failed to fetch approval status: ${e.message}")
            }
        )
    }

    fun fetchStudentCurriculum(studentId: String) {
        repository.getStudentCurriculum(
            studentId,
            onComplete = { curriculumId ->
                _studentCurriculum.postValue(curriculumId)  // 🔥 Use postValue to ensure UI update
                Log.d("AdminViewModel", "Curriculum for student $studentId: $curriculumId")
            },
            onError = { e ->
                Log.e("AdminViewModel", "Failed to fetch curriculum: ${e.message}")
            }
        )
    }

    fun updateStudentApprovalStatus(studentId: String, status: String) {
        repository.updateStudentApprovalStatus(studentId, status)
            .addOnSuccessListener {
                Log.d("AdminViewModel", "Approval status updated successfully to $status for student $studentId")
                _studentApprovalStatus.value = status  // ✅ Update locally first
                viewModelScope.launch {
                    delay(1000) // ✅ Give Firestore time to sync
                    fetchStudentApprovalStatus(studentId) // ✅ Force re-fetch to avoid stale data
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminViewModel", "Error updating approval status", e)
            }
    }

    fun fetchStudentProgram(studentId: String) {
        repository.fetchStudentProgram(studentId) { program ->
            _studentProgram.value = program
        }
    }

    //Admin page functinos
    // fetch admin email
    private val _adminEmail = MutableLiveData<String>()
    val adminEmail: LiveData<String> = _adminEmail

    fun fetchAdminEmail(userID: String) {
        repository.fetchAdminEmail(userID) { email ->
            _adminEmail.postValue(email) // ✅ Use postValue for LiveData
            Log.d("AdminEmail", "Admin Email Fetched for Homepage: $email")

        }

    }


    fun logoutAdmin() {
        TODO("Not yet implemented")
    }
}