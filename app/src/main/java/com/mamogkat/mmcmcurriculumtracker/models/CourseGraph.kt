package com.mamogkat.mmcmcurriculumtracker.models

class CourseGraph(
    val groupedCourses: Map<Int, Map<Int, List<CourseNode>>>, // ✅ Organized by Year → Term
    val electives: List<CourseNode> = emptyList() // ✅ Separate list for electives
) {

    private val adjacencyList = mutableMapOf<String, MutableList<String>>() // Key: Course code, Value: List of dependent courses
    private val courses = mutableMapOf<String, CourseNode>() // Store course details

    init {
        groupedCourses.values.forEach { termMap ->
            termMap.values.flatten().forEach { addCourse(it) } // ✅ Populate graph with all courses
        }
        electives.forEach { addCourse(it) } // ✅ Include electives
    }

    fun addCourse(course: CourseNode) {
        courses[course.code] = course
        adjacencyList.putIfAbsent(course.code, mutableListOf())
    }

    fun addPrerequisite(courseCode: String, prerequisite: String) {
        adjacencyList.putIfAbsent(courseCode, mutableListOf())
        adjacencyList[prerequisite]?.add(courseCode)
    }

    fun getNextAvailableCourses(enrolledTerm: Int, completedCourses: Set<String>): List<Pair<CourseNode, String>> {
        val availableCourses = mutableListOf<Pair<CourseNode, String>>() // Pair (course, colorCode)

        for ((courseCode, course) in courses) {
            if (completedCourses.contains(courseCode)) continue

            val prerequisitesMet = course.prerequisites.all { completedCourses.contains(it) }

            val colorCode = when {
                !prerequisitesMet -> "red" // Prerequisites not met
                enrolledTerm in course.regularTerms -> "green" // eligible and offered this term
                else -> "orange" // eligible but not regularly offered this term
            }
            availableCourses.add(Pair(course, colorCode))
        }
        return availableCourses
    }
}
