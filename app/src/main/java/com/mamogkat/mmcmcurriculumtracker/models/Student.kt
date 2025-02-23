package com.mamogkat.mmcmcurriculumtracker.models

data class Student(
    val userID: String = "",  // for mapping to Firebase
    val studentID: String = "",         // Unique identifier for the student
    val name: String = "Unknown",           // Student's name
    val email: String = "",          // Student's email
    val studentNumber: String = "",  // Student number
    val program: String = "",        // Program (e.g., "BS Computer Engineering")
    val termEnrolling: Int = 1,  // The term the student is enrolling in
    val curriculum: String? = null,     // Curriculum ID or name
    val completedCourses: List<String> = emptyList(), // List of completed course IDs
    val approvalStatus: String = ""  // Approval status (e.g., "Pending", "Approved", "Rejected")
){
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "Unknown", "", "", "", 1, null, emptyList(), "")
}
