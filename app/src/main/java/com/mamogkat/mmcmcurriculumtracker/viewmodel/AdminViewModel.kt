package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mamogkat.mmcmcurriculumtracker.models.Curriculum
import com.mamogkat.mmcmcurriculumtracker.models.Student
import com.mamogkat.mmcmcurriculumtracker.repository.FirebaseRepository

class AdminViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _studentList = MutableLiveData<List<Student>>()
    val studentList: LiveData<List<Student>> = _studentList

    private val _curriculumList = MutableLiveData<List<Curriculum>>()
    val curriculumList: LiveData<List<Curriculum>> = _curriculumList

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

    fun logoutAdmin() {
        TODO("Not yet implemented")
    }
}