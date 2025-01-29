package com.mamogkat.mmcmcurriculumtracker.models

class CourseGraph (private val courses: List<CourseNode>){
    private val courseMap = courses.associateBy { it.code }

    fun getNextCourses(completedCourses: Set<String>) : List<CourseNode>{
        return courses.filter { course ->
            course.prerequisites.all { completedCourses.contains(it) }
        }
    }

    fun getTotalUnits(completedCourses: Set<String>) : Double {
        return courses.filter { completedCourses.contains(it.code) }
            .sumOf { it.units }
    }

    fun getCourseDetails(courseCode: String) : CourseNode? {
        return courseMap[courseCode]
    }
}
