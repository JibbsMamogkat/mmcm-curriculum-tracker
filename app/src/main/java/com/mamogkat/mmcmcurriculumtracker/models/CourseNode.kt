package com.mamogkat.mmcmcurriculumtracker.models

data class CourseNode(
    val code: String = "",
    val name: String = "",
    val units: Int = 0,
    val prerequisites: List<String> = listOf(),
    val corequisites: List<String> = listOf(),
    val regularTerms: List<Int> = listOf(),
    val taken: Boolean = false
)
