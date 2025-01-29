package com.mamogkat.mmcmcurriculumtracker.models

data class CourseNode(
    val code: String,
    val title: String,
    val units: Double,
    val prerequisites: List<String> = listOf()
)