package com.mamogkat.mmcmcurriculumtracker.models

data class Student(
    val userID: String = "",  // for mapping to Firebase
    val studentID: String = "",         // Unique identifier for the student
    val name: String = "Unknown",           // Student's name
    val email: String = "",          // Student's email
    val studentNumber: String = "",  // Student number
    val program: String = "",        // Program (e.g., "BS Computer Engineering")
    val termEnrolling: String = "",  // The term the student is enrolling in
    val curriculum: String? = null,     // Curriculum ID or name
    val coursesCompleted: List<String> = emptyList() // List of completed course IDs

){
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "Unknown", "", "", "", "", null, emptyList())
}
