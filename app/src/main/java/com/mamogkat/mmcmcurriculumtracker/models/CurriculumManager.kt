package com.mamogkat.mmcmcurriculumtracker.models

class CurriculumManager{
    private val curriculums = mutableListOf<Curriculum>()

    fun addCurriculum(curriculum: Curriculum){
        curriculums.add(curriculum)
    }

    fun getCurriculum(program: String, year: Int) : Curriculum? {
        return curriculums.find { it.program == program && it.year == year }
    }

//    fun getTotalUnits(program: String, year: Int, completedCourses: Set<String>) : Int? {
//        val curriculum = getCurriculum(program, year) ?: return null
//        val courseGraph = CourseGraph(curriculum.courses)
//        return courseGraph.getTotalUnits(completedCourses)
//    }
}