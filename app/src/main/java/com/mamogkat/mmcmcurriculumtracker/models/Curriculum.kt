package com.mamogkat.mmcmcurriculumtracker.models

data class Curriculum(
    val curriculumID: String,
    val name: String,
    val program: String,
    val year: String,
    val courses: List<CourseNode>
) {
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "", "", emptyList())
}
