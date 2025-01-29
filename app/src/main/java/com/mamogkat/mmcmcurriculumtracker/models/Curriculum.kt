package com.mamogkat.mmcmcurriculumtracker.models

data class Curriculum(
    val program: String,
    val year: Int,
    val courses: List<CourseNode>
)