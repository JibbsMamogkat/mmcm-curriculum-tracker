package com.mamogkat.mmcmcurriculumtracker.repository

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    fun uploadCourses(
        program: String,
        year: Int,
        term: String,
        courses: List<Map<String, Any>>,
        OnCompleted: () -> Unit
    ) {
        val termRef = db.collection("curriculums")
            .document(program)
            .collection(year.toString())
            .document(term)

        courses.forEach { course ->
            termRef.collection("courses")
                .document(course["code"].toString())
                .set(course)
                .addOnSuccessListener {
                    // Handle success
                    println("{Course ${course["code"]} uploaded successfully")
                }
                .addOnFailureListener() {
                    // Handle error
                    println("Error uploading course")
                }
        }
    }
}