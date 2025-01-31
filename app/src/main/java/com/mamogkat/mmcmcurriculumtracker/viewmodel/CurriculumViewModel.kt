package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mamogkat.mmcmcurriculumtracker.repository.FirebaseRepository

class CurriculumViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    fun uploadFirstYearTerm1() {
        val firstYearTerm1Courses = listOf(
            mapOf(
                "code" to "CHM031",
                "name" to "Chemistry for Engineers",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CHM031L",
                "name" to "Chemistry for Engineers (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf("CHM031")
            ),
            mapOf(
                "code" to "ENG023",
                "name" to "Receptive Communication Skills",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "HUM021",
                "name" to "Logic and Critical Thinking",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH031",
                "name" to "Mathematics for Engineers",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SS022",
                "name" to "Readings in Philippine History",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "NSTP010",
                "name" to "National Service Training Program 1",
                "units" to -3, // (3.0) but no actual credit, stored as -3 for distinction
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "VE021",
                "name" to "Life Coaching Series 1",
                "units" to -1, // (1.0) but no actual credit, stored as -1 for distinction
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 1, "term_1", firstYearTerm1Courses){
            // Handle completion
            Log.d("UploadDebug", "First Year Term 1 courses uploaded")
        }
    }

    fun uploadFirstYearTerm2() {
        val firstYearTerm2Courses = listOf(
            mapOf(
                "code" to "CPE001L",
                "name" to "Computer Fundamentals and Programming 1 (Lab)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE100",
                "name" to "Computer Engineering as a Discipline",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "DRAW021W",
                "name" to "Engineering Drawing 1",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "ENG024",
                "name" to "Writing for Academic Studies",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH035",
                "name" to "Mathematics in the Modern World",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH041",
                "name" to "Engineering Calculus 1",
                "units" to 4,
                "prerequisites" to listOf("MATH031"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SS021",
                "name" to "Understanding the Self",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "NSTP011P",
                "name" to "National Service Training Program 2 (Paired)",
                "units" to -3,
                "prerequisites" to listOf("NSTP010"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "VE022",
                "name" to "Life Coaching Series 2",
                "units" to -1,
                "prerequisites" to listOf("VE021"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 1, "term_2", firstYearTerm2Courses){
            // Handle completion
            Log.d("UploadDebug", "First Year Term 2 courses uploaded")
        }
    }
}

