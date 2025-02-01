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
    fun uploadElectives(program: String, electives: Map<String, List<Map<String, Any>>>) {
        electives.forEach { (category, courses) ->
            val categoryRef = db.collection("curriculums")
                .document(program)
                .collection("electives")
                .document(category)

            courses.forEach { course ->
                categoryRef.collection("courses")
                    .document(course["code"].toString())
                    .set(course)
                    .addOnSuccessListener {
                        println("Elective ${course["code"]} uploaded successfully")
                    }
                    .addOnFailureListener { e ->
                        println("Error uploading elective ${course["code"]}: ${e.message}")
                    }
            }
        }
    }
}