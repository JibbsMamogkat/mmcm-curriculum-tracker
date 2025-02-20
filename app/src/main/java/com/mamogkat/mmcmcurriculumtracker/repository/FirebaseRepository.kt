package com.mamogkat.mmcmcurriculumtracker.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.models.Curriculum
import com.mamogkat.mmcmcurriculumtracker.models.Student

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

    fun updateCoursesWithRegularTerms(program: String) {
        val dbRef = db.collection("curriculums").document(program)

        for (year in 1..4) {
            for (term in 1..3) {
                val termRef = dbRef.collection(year.toString()).document("term_$term").collection("courses")

                termRef.get().addOnSuccessListener { documents ->
                    for (doc in documents) {
                        val courseCode = doc.id

                        // 🔥 Assign the current term as the only regular term
                        val regularTerms = listOf(term)

                        // Update Firestore with regularTerms
                        termRef.document(courseCode)
                            .update("regularTerms", regularTerms)
                            .addOnSuccessListener {
                                Log.d("FirestoreUpdate", "Updated $courseCode with regularTerms: $regularTerms")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreUpdate", "Failed to update $courseCode: ${e.message}")
                            }
                    }
                }
            }
        }
    }
    fun updateElectivesWithRegularTerms(program: String) {
        val electivesRef = db.collection("curriculums").document(program).collection("electives")

        // 🔥 Corrected elective categories with "P" instead of "F"
        val electiveCategories = listOf("AWS171P", "EMSY171P", "GEN_ED", "MACH171P", "MICR172P", "NETA172P", "SDEV173P", "SNAD174P")

        for (categoryName in electiveCategories) {
            val coursesRef = electivesRef.document(categoryName).collection("courses")

            coursesRef.get().addOnSuccessListener { electiveDocs ->
                if (electiveDocs.isEmpty) {
                    Log.e("FirestoreUpdate", "⚠️ No courses found in category: $categoryName")
                } else {
                    Log.d("FirestoreUpdate", "✅ Courses in $categoryName: ${electiveDocs.documents.map { it.id }}")
                }

                // Update each elective course with "regularTerms": [1, 2, 3]
                for (doc in electiveDocs.documents) {
                    val courseCode = doc.id
                    val regularTerms = listOf(1, 2, 3) // Electives are available in all terms

                    coursesRef.document(courseCode)
                        .update("regularTerms", regularTerms)
                        .addOnSuccessListener {
                            Log.d("FirestoreUpdate", "✅ Successfully updated $courseCode")
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreUpdate", "❌ Failed to update $courseCode: ${e.message}")
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("FirestoreUpdate", "🔥 Failed to fetch courses in category $categoryName: ${e.message}")
            }
        }
    }

    fun getAllStudents(callback: (List<Student>) -> Unit) {
        Log.d("Firestore", "Fetching all students from Firestore...")

        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                Log.d("Firestore", "Successfully fetched ${result.size()} students.")

                val students = result.mapNotNull { document ->
                    try {
                        val student = document.toObject(Student::class.java)?.copy(studentID = document.id)
                        Log.d("Firestore", "Fetched Student ID: ${document.id}, Name: ${student?.name}, Email: ${student?.email}, ApprovalStatus: ${student?.approvalStatus}")
                        student
                    } catch (e: Exception) {
                        Log.e("Firestore", "Failed to deserialize student: ${document.id}", e)
                        null
                    }
                }

                Log.d("Firestore", "Total students after filtering: ${students.size}")
                callback(students)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching students", exception)
            }
    }


    fun getAllCurriculums(callback: (List<Curriculum>) -> Unit) {
        db.collection("curriculums")
            .get()
            .addOnSuccessListener { result ->
                val curriculums = result.mapNotNull { document ->
                    try {
                        document.toObject(Curriculum::class.java)
                    } catch (e: Exception) {
                        Log.e("Firestore", "Failed to deserialize curriculum: ${document.id}", e)
                        null
                    }
                }
                callback(curriculums)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching curriculums", exception)
            }
    }

    fun updateStudentCurriculum(studentId: String, newCurriculumId: String, callback: (Boolean) -> Unit) {
        if (studentId.isEmpty()) {
            Log.e("Firebase", "Error: studentId is empty, cannot update curriculum")
            callback(false)
            return
        }

        db.collection("students").document(studentId)
            .update("curriculum", newCurriculumId)
            .addOnSuccessListener {
                Log.d("Firebase", "Updated curriculum for student: $studentId")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error updating curriculum: $studentId", exception)
                callback(false)
            }
    }



    fun removeStudent(studentId: String) {
        db.collection("students").document(studentId).delete()
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "Student removed successfully")
            }
            .addOnFailureListener {
                Log.e("FirebaseRepository", "Failed to remove student", it)
            }
    }


    //functions for curriculum admin overview screen
    fun getStudentDocument(studentId: String): DocumentReference {
        return db.collection("students").document(studentId)
    }

    fun getCurriculumDocument(curriculumId: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("curriculums")
            .document(curriculumId)
    }


}


