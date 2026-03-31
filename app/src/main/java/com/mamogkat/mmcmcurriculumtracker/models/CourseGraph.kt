package com.mamogkat.mmcmcurriculumtracker.models

import android.util.Log
import java.util.PriorityQueue

class CourseGraph(
    val groupedCourses: Map<Int, Map<Int, List<CourseNode>>>, // ✅ Organized by Year → Term
    val electives: List<CourseNode> = emptyList() // ✅ Separate list for electives
) {

    private val adjacencyList = mutableMapOf<String, MutableList<String>>() // Key: Course code → List of dependent courses
    private val courses = mutableMapOf<String, CourseNode>() // Stores course details
    private val inDegree = mutableMapOf<String, Int>() // Stores prerequisites count

    // ✅ Fast lookup set to easily check if a course is an elective later
    private val electiveCodes = electives.map { it.code }.toSet()

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
        } // ✅ Include electives so Kahn's algorithm respects their prerequisites
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

        // ✅ PriorityQueue ensures proper ordering (lowest year first, then lowest term)
        val queue = PriorityQueue<CourseNode> { a, b ->
            when {
                a.yearLevel != b.yearLevel -> a.yearLevel - b.yearLevel  // 🔹 Sort by year level first
                a.term != b.term -> a.term - b.term                      // 🔹 Then by term
                else -> a.code.compareTo(b.code)                         // 🔹 Lastly, sort alphabetically
            }
        }

        Log.d("CourseGraph", "Initializing queue with courses that have all prerequisites completed...")

        // Step 1: Initialize queue with courses that have **all prerequisites completed**
        for ((courseCode, course) in courses) {
            val prerequisitesMet = course.prerequisites.all { completedCourses.contains(it) }

            if (prerequisitesMet && courseCode !in completedCourses) {
                queue.add(course)
            }
        }
        Log.d("CourseGraph", "Total courses added to queue: ${queue.size}")

        Log.d("CourseGraph", "Processing courses using Kahn’s Algorithm...")

        // Step 2: Process courses using Kahn’s Algorithm with priority queue
        while (queue.isNotEmpty()) {
            val course = queue.poll() // 🔹 Get the highest-priority course
            val courseCode = course.code

            // Step 3: Determine eligibility color using the fast lookup set
            val colorCode = when {
                courseCode in electiveCodes -> "blue" // ✅ Electives are strictly blue
                enrolledTerm in course.regularTerms -> "green" // ✅ Non-elective eligible & offered this term
                else -> "orange" // ✅ Non-elective eligible but not regularly offered this term
            }

            availableCourses.add(Pair(course, colorCode))

            // Step 4: Reduce in-degree for dependent courses **ONLY if prerequisites are met**
            adjacencyList[courseCode]?.forEach { dependent ->
                inDegree[dependent] = inDegree.getOrDefault(dependent, 0) - 1

                val dependentCourse = courses[dependent]
                if (dependentCourse != null) {
                    val allPrerequisitesMet = dependentCourse.prerequisites.all { completedCourses.contains(it) }

                    if (inDegree[dependent] == 0 && allPrerequisitesMet && dependent !in completedCourses) {
                        queue.add(dependentCourse) // 🔹 Add to priority queue to maintain sorting
                    }
                }
            }
        }

        // ❌ Step 5 removed entirely to prevent duplicate courses from being added.
        Log.d("CourseGraph", "Total available courses found: ${availableCourses.size}")

        return availableCourses
    }
}