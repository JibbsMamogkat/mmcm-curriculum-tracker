package com.mamogkat.mmcmcurriculumtracker.models

data class CourseNode(
    val code: String = "",
    val name: String = "",
    val units: Int = 0,
    val yearLevel: Int = 0, // ✅ Default value added
    val term: Int = 0, // ✅ Default value added
    val prerequisites: List<String> = emptyList(),
    val coRequisites: List<String> = emptyList(),
    val regularTerms: List<Int> = emptyList(),
    val taken: Boolean = false
) {
    // 🔥 Firestore requires an empty constructor
    constructor() : this("", "", 0, 0, 0, emptyList(), emptyList(), emptyList(), false)
}
