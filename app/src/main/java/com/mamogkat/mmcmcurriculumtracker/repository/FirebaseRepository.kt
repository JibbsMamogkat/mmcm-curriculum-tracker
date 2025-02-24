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
import kotlinx.coroutines.tasks.await

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

    fun updateCoursesWithYearLevelAndTerm(program: String) {
        val dbRef = db.collection("curriculums").document(program)

        for (year in 1..4) {
            for (term in 1..3) {
                val termRef = dbRef.collection(year.toString()).document("term_$term").collection("courses")

                termRef.get().addOnSuccessListener { documents ->
                    for (doc in documents) {
                        val courseCode = doc.id

                        // 🔥 Assign yearLevel and term based on current loop iteration
                        val updates = mapOf(
                            "yearLevel" to year,
                            "term" to term
                        )

                        // Update Firestore with yearLevel and term
                        termRef.document(courseCode)
                            .update(updates)
                            .addOnSuccessListener {
                                Log.d("FirestoreUpdate", "Updated $courseCode with yearLevel: $year, term: $term")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreUpdate", "Failed to update $courseCode: ${e.message}")
                            }
                    }
                }.addOnFailureListener { e ->
                    Log.e("FirestoreUpdate", "Failed to fetch courses for Year $year, Term $term: ${e.message}")
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

    fun updateElectivesWithYearLevelAndTerm(program: String) {
        val electivesRef = db.collection("curriculums").document(program).collection("electives")

        // 🔥 Corrected elective categories with "P" instead of "F"
        val electiveCategories = listOf(
            "AWS171P", "EMSY171P", "GEN_ED", "MACH171P", "MICR172P", "NETA172P", "SDEV173P", "SNAD174P"
        )

        for (categoryName in electiveCategories) {
            val coursesRef = electivesRef.document(categoryName).collection("courses")

            coursesRef.get().addOnSuccessListener { electiveDocs ->
                if (electiveDocs.isEmpty) {
                    Log.e("FirestoreUpdate", "⚠️ No courses found in category: $categoryName")
                } else {
                    Log.d("FirestoreUpdate", "✅ Courses in $categoryName: ${electiveDocs.documents.map { it.id }}")
                }

                // Determine the correct yearLevel and term
                val yearLevel = if (categoryName == "GEN_ED") 4 else 3
                val term = if (categoryName == "GEN_ED") 4 else 3

                // Update each elective course
                for (doc in electiveDocs.documents) {
                    val courseCode = doc.id
                    val updates = mapOf(
                        "yearLevel" to yearLevel,
                        "term" to term
                    )

                    coursesRef.document(courseCode)
                        .update(updates)
                        .addOnSuccessListener {
                            Log.d("FirestoreUpdate", "✅ Updated $courseCode with yearLevel: $yearLevel, term: $term")
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

//    fun updateStudentCurriculum(studentId: String, newCurriculumId: String, callback: (Boolean) -> Unit) {
//        if (studentId.isEmpty()) {
//            Log.e("StudentCardBug", "Error: studentId is empty, cannot update curriculum")
//            callback(false)
//            return
//        }
//
//        db.collection("students").document(studentId)
//            .update("curriculum", newCurriculumId)
//            .addOnSuccessListener {
//                Log.d("StudentCardBug", "Updated curriculum for student: $studentId")
//                callback(true)
//            }
//            .addOnFailureListener { exception ->
//                Log.e("StudentCardBug", "Error updating curriculum: $studentId", exception)
//                callback(false)
//            }
//    }

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

    fun getStudentEmail(studentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("students")
            .document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val email = document.getString("email") ?: "No Email Found"
                    onSuccess(email) // Return email if found
                } else {
                    onSuccess("No Email Found") // Default value
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Handle failure
            }
    }

    suspend fun getStudentCompletedCourses(studentId: String): Set<String> {
        return try {
            val snapshot = db.collection("students")
                .document(studentId)
                .get()
                .await() // Suspend function to fetch data asynchronously

            if (snapshot.exists()) {
                val completedCourses = snapshot.get("completedCourses") as? List<String> ?: emptyList()
                Log.d("FirebaseRepository", "Fetched completed courses for $studentId: $completedCourses")
                completedCourses.toSet() // Convert to Set<String> for consistency
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error fetching completed courses for $studentId", e)
            emptySet()
        }
    }

    fun getStudentApprovalStatus(studentId: String, onComplete: (String) -> Unit, onError: (Exception) -> Unit) {
        db.collection("students").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val status = document.getString("approvalStatus") ?: ""
                    onComplete(status)
                    Log.d("FirebaseRepository", "Fetched approval status: $status for student $studentId")
                } else {
                    Log.e("FirebaseRepository", "Student document not found for ID: $studentId")
                    onComplete("")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepository", "Error fetching approval status: ${e.message}")
                onError(e)
            }
    }

    fun getStudentCurriculum(studentId: String, onComplete: (String) -> Unit, onError: (Exception) -> Unit) {
        db.collection("students").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val curriculum = document.getString("curriculum") ?: ""
                    onComplete(curriculum)
                } else {
                    onComplete("")
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }


    fun updateCompletedCourses(studentId: String, completedCourses: Set<String>) {
        db.collection("students").document(studentId)
            .update("completedCourses", completedCourses.toList())
            .addOnSuccessListener {
                Log.d("FirebaseRepository", "Updated completedCourses: $completedCourses")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepository", "Error updating completed courses", e)
            }
    }

    fun updateStudentApprovalStatus(studentId: String, status: String): Task<Void> {
        return db.collection("students")
            .document(studentId)
            .update("approvalStatus", status)
    }

    fun updateStudentCurriculum(studentId: String, curriculumID: String): Task<Void> {
        return db.collection("students")
            .document(studentId)
            .update("curriculum", curriculumID)
    }

    fun fetchStudentProgram(studentId: String, onResult: (String) -> Unit) {
        db.collection("students").document(studentId)
            .get()
            .addOnSuccessListener { document ->
                val program = document.getString("program") ?: "Unknown"
                onResult(program)
            }
            .addOnFailureListener { onResult("Unknown") }
    }

    fun updateStudentProgram(studentId: String, program: String): Task<Void> {
        return db.collection("students")
            .document(studentId)
            .update("program", program)
    }


    fun fetchAdminEmail(userID: String, onResult: (String) -> Unit) {
        db.collection("admins").document(userID)
            .get()
            .addOnSuccessListener { document ->
                Log.d("AdminEmail", "Document Data: ${document.data}")
                val email = document.getString("email") ?: "Unknown"
                Log.d("AdminEmail", "Extracted Email: $email")
                onResult(email)
    }

    }//end of class
}



