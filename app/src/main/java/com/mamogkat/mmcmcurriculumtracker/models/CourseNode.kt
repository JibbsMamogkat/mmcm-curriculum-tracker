package com.mamogkat.mmcmcurriculumtracker.models

data class CourseNode(
    val code: String = "",
    val name: String = "",
    val units: Int = 0,
    val prerequisites: List<String> = emptyList(),
    val corequisites: List<String> = emptyList(),
    val taken: Boolean = false
)
