package com.mamogkat.mmcmcurriculumtracker.models

import android.util.Log

class CourseGraph(
    val groupedCourses: Map<Int, Map<Int, List<CourseNode>>>, // ✅ Organized by Year → Term
    val electives: List<CourseNode> = emptyList() // ✅ Separate list for electives
) {

    private val adjacencyList = mutableMapOf<String, MutableList<String>>() // Key: Course code → List of dependent courses
    private val courses = mutableMapOf<String, CourseNode>() // Stores course details
    private val inDegree = mutableMapOf<String, Int>() // Stores prerequisites count

    init {
        Log.d("CourseGraph", "Initializing CourseGraph with grouped courses and electives.")
        Log.d("CourseGraph", "Populating graph with courses...")
        // ✅ Populate Graph with Courses
        groupedCourses.values.forEach { termMap ->
            termMap.values.flatten().forEach {
                addCourse(it)
            }
        }
        Log.d("CourseGraph", "Total courses added to graph: ${courses.size}")
        Log.d("CourseGraph", "Populating graph with electives...")
        electives.forEach {
            addCourse(it)
        } // ✅ Include electives
        Log.d("CourseGraph", "Total electives added to graph: ${electives.size}")
    }

    fun addCourse(course: CourseNode) {
        courses[course.code] = course
        adjacencyList.putIfAbsent(course.code, mutableListOf())
        inDegree[course.code] = 0 // Initialize in-degree for each course

        for (prerequisite in course.prerequisites) {
            if (prerequisite in courses) {
                adjacencyList.putIfAbsent(prerequisite, mutableListOf())
                adjacencyList[prerequisite]?.add(course.code)
                inDegree[course.code] = inDegree.getOrDefault(course.code, 0) + 1
            }
        }
    }

    fun addPrerequisite(courseCode: String, prerequisite: String) {
        Log.d("CourseGraph", "Adding prerequisite: $prerequisite -> $courseCode")

        adjacencyList.putIfAbsent(courseCode, mutableListOf())
        adjacencyList[prerequisite]?.add(courseCode)
        inDegree[courseCode] = inDegree.getOrDefault(courseCode, 0) + 1
    }

    fun getNextAvailableCourses(enrolledTerm: Int, completedCourses: Set<String>): List<Pair<CourseNode, String>> {
        Log.d("CourseGraph", "Fetching next available courses for enrolledTerm: $enrolledTerm")
        Log.d("CourseGraph", "Completed courses: $completedCourses")

        val availableCourses = mutableListOf<Pair<CourseNode, String>>() // Stores available courses with color codes

        Log.d("CourseGraph", "Initializing queue with courses that have all prerequisites completed...")

        // Step 1: Initialize queue with courses that have **all prerequisites completed**
        val queue = ArrayDeque<String>()
        for ((courseCode, course) in courses) {
            val prerequisitesMet = course.prerequisites.all { completedCourses.contains(it) }

            if (prerequisitesMet && courseCode !in completedCourses) {
                queue.add(courseCode)
            }
        }
        Log.d("CourseGraph", "Total courses added to queue: ${queue.size}")
        Log.d("CourseGraph", "Processing courses using Kahn’s Algorithm...")
        // Step 2: Process courses using Kahn’s Algorithm
        while (queue.isNotEmpty()) {
            val courseCode = queue.removeFirst()
            val course = courses[courseCode] ?: continue

            // Step 3: Determine eligibility color
            val colorCode = when {
                enrolledTerm in course.regularTerms -> "green" // ✅ Eligible & offered this term
                else -> "orange" // ✅ Eligible but not regularly offered this term
            }

            availableCourses.add(Pair(course, colorCode))

            // Step 4: Reduce in-degree for dependent courses **ONLY if prerequisites are met**
            adjacencyList[courseCode]?.forEach { dependent ->
                inDegree[dependent] = inDegree.getOrDefault(dependent, 0) - 1

                val dependentCourse = courses[dependent]
                if (dependentCourse != null) {
                    val allPrerequisitesMet = dependentCourse.prerequisites.all { completedCourses.contains(it) }

                    if (inDegree[dependent] == 0 && allPrerequisitesMet && dependent !in completedCourses) {
                        queue.add(dependent)
                    }
                }
            }
        }
        Log.d("CourseGraph", "Total available non-elective courses found: ${availableCourses.size}")
        Log.d("CourseGraph", "Processing electives...")

        // Step 5: Ensure electives are always available if not completed
        electives.forEach { elective ->
            if (elective.code !in completedCourses) {
                availableCourses.add(Pair(elective, "blue")) // ✅ Special case for electives
            }
        }
        Log.d("CourseGraph", "Total electives processed: ${electives.size}")

        Log.d("CourseGraph", "Total available courses found: ${availableCourses.size}")

        return availableCourses
    }

}

