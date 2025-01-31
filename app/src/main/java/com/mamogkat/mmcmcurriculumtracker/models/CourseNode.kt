package com.mamogkat.mmcmcurriculumtracker.models

data class CourseNode(
    val code: String = "",
    val name: String = "",
    val units: Double = 0.0,
    val prerequisites: List<String> = emptyList(),
    val corequisites: List<String> = emptyList()
)
