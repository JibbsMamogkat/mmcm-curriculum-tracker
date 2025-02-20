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

        // ✅ Populate Graph with Courses
        groupedCourses.values.forEach { termMap ->
            termMap.values.flatten().forEach {
                Log.d("CourseGraph", "Adding course: ${it.code}")
                addCourse(it)
            }
        }
        electives.forEach {
            Log.d("CourseGraph", "Adding elective: ${it.code}")
            addCourse(it)
        } // ✅ Include electives
    }

    fun addCourse(course: CourseNode) {
        Log.d("CourseGraph", "Adding course to graph: ${course.code}")

        courses[course.code] = course
        adjacencyList.putIfAbsent(course.code, mutableListOf())
        inDegree[course.code] = 0 // Initialize in-degree for each course

        for (prerequisite in course.prerequisites) {
            Log.d("CourseGraph", "Processing prerequisite for ${course.code}: $prerequisite")

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

        // Step 1: Initialize queue with courses that have **all prerequisites completed**
        val queue = ArrayDeque<String>()
        for ((courseCode, course) in courses) {
            val prerequisitesMet = course.prerequisites.all { completedCourses.contains(it) }

            if (prerequisitesMet && courseCode !in completedCourses) {
                Log.d("CourseGraph", "Adding to queue: $courseCode (All prerequisites met)")
                queue.add(courseCode)
            } else {
                Log.d("CourseGraph", "Skipping $courseCode, unmet prerequisites: ${course.prerequisites.filter { it !in completedCourses }}")
            }
        }

        // Step 2: Process courses using Kahn’s Algorithm
        while (queue.isNotEmpty()) {
            val courseCode = queue.removeFirst()
            val course = courses[courseCode] ?: continue

            // Step 3: Determine eligibility color
            val colorCode = when {
                enrolledTerm in course.regularTerms -> "green" // ✅ Eligible & offered this term
                else -> "orange" // ✅ Eligible but not regularly offered this term
            }

            Log.d("CourseGraph", "Course added to available list: ${course.code} with color $colorCode")
            availableCourses.add(Pair(course, colorCode))

            // Step 4: Reduce in-degree for dependent courses **ONLY if prerequisites are met**
            adjacencyList[courseCode]?.forEach { dependent ->
                inDegree[dependent] = inDegree.getOrDefault(dependent, 0) - 1

                val dependentCourse = courses[dependent]
                if (dependentCourse != null) {
                    val allPrerequisitesMet = dependentCourse.prerequisites.all { completedCourses.contains(it) }

                    if (inDegree[dependent] == 0 && allPrerequisitesMet && dependent !in completedCourses) {
                        Log.d("CourseGraph", "Adding dependent course to queue: $dependent")
                        queue.add(dependent)
                    } else {
                        Log.d("CourseGraph", "Skipping dependent course $dependent, unmet prerequisites: ${dependentCourse.prerequisites.filter { it !in completedCourses }}")
                    }
                }
            }
        }

        // Step 5: Ensure electives are always available if not completed
        electives.forEach { elective ->
            if (elective.code !in completedCourses) {
                Log.d("CourseGraph", "Adding elective to available courses: ${elective.code}")
                availableCourses.add(Pair(elective, "blue")) // ✅ Special case for electives
            }
        }

        Log.d("CourseGraph", "Total available courses found: ${availableCourses.size}")
        return availableCourses
    }

}

