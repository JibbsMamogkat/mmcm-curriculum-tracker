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

    private val _studentApprovalStatus = mutableStateMapOf<String, String>()
    val studentApprovalStatus: Map<String, String> get() = _studentApprovalStatus

    private val _studentCurriculum = mutableStateMapOf<String, String>()
    val studentCurriculum: Map<String, String> get() = _studentCurriculum

    private val _studentProgram = mutableStateMapOf<String, String>()
    val studentProgram: Map<String, String> get() = _studentProgram


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


//    fun updateStudentCurriculum(studentId: String?, newCurriculumId: String) {
//        if (studentId.isNullOrEmpty()) {
//            Log.e("StudentCardBug", "Error: studentId is null or empty, cannot update curriculum")
//            return
//        }
//
//        repository.updateStudentCurriculum(studentId, newCurriculumId) { success ->
//            if (success) {
//                Log.d("StudentCardBug", "Successfully updated curriculum for student: $studentId")
//                fetchStudents() //refresh the student list to trigger UI recomposition
//            } else {
//                Log.e("StudentCardBug", "Failed to update curriculum for student: $studentId")
//            }
//        }
//    }

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
                _studentApprovalStatus[studentId] = status // 🔥 Use postValue to ensure UI update
                Log.d("StudentCardBug", "Approval status for student $studentId: $status")
            },
            onError = { e ->
                Log.e("StudentCardBug", "Failed to fetch approval status: ${e.message}")
            }
        )
    }

    fun fetchStudentCurriculum(studentId: String) {
        repository.getStudentCurriculum(
            studentId,
            onComplete = { curriculumId ->
                _studentCurriculum[studentId] = curriculumId // 🔥 Use postValue to ensure UI update
                Log.d("StudentCardBug", "Curriculum for student $studentId: $curriculumId")
            },
            onError = { e ->
                Log.e("StudentCardBug", "Failed to fetch curriculum: ${e.message}")
            }
        )
    }

    fun updateStudentApprovalStatus(studentId: String, status: String) {
        repository.updateStudentApprovalStatus(studentId, status)
            .addOnSuccessListener {
                Log.d("StudentCardBug", "Approval status updated successfully to $status for student $studentId")
                _studentApprovalStatus[studentId] = status  // ✅ Update locally first
                viewModelScope.launch {
                    delay(1000) // ✅ Give Firestore time to sync
                    fetchStudentApprovalStatus(studentId) // ✅ Force re-fetch to avoid stale data
                }
            }
            .addOnFailureListener { e ->
                Log.e("StudentCardBug", "Error updating approval status", e)
            }
    }

    fun updateStudentCurriculum(studentId: String, curriculumID: String) {
        repository.updateStudentCurriculum(studentId, curriculumID)
            .addOnSuccessListener {
                Log.d("StudentCardBug", "Curriculum updated successfully to $curriculumID for student $studentId")
                _studentCurriculum[studentId] = curriculumID  // ✅ Update locally first
                viewModelScope.launch {
                    delay(1000) // ✅ Give Firestore time to sync
                    fetchStudentCurriculum(studentId) // ✅ Force re-fetch to avoid stale data
                }
            }
            .addOnFailureListener { e ->
                Log.e("StudentCardBug", "Error updating Student Curriculum", e)
            }
    }

    fun fetchStudentProgram(studentId: String) {
        repository.fetchStudentProgram(studentId) { program ->
            _studentProgram[studentId] = program
            Log.d("StudentCardBug", "Program for student $studentId: $program")
        }
    }

    fun updateStudentProgram(studentId: String, program: String) {
        repository.updateStudentProgram(studentId, program)
            .addOnSuccessListener {
                Log.d("StudentCardBug", "Programupdated successfully to $program for student $studentId")
                _studentProgram[studentId] = program  // ✅ Update locally first
                viewModelScope.launch {
                    delay(1000) // ✅ Give Firestore time to sync
                    fetchStudentProgram(studentId) // ✅ Force re-fetch to avoid stale data
                }
            }
            .addOnFailureListener { e ->
                Log.e("StudentCardBug", "Error updating program", e)
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