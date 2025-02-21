package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mamogkat.mmcmcurriculumtracker.models.CourseGraph
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.repository.FirebaseRepository

class CurriculumViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _courseGraph = MutableLiveData<CourseGraph>()
    val courseGraph: LiveData<CourseGraph> = _courseGraph

    private val _completedCourses = MutableLiveData<Set<String>>()
    val completedCourses: LiveData<Set<String>> = _completedCourses

    private val _enrolledTerm = MutableLiveData<Int>()
    val enrolledTerm: LiveData<Int> = _enrolledTerm

    private val _selectedCurriculum = MutableLiveData<String>()
    val selectedCurriculum: LiveData<String> = _selectedCurriculum

    private val _studentEmail = MutableLiveData<String>()
    val studentEmail: LiveData<String> get() = _studentEmail

    fun setEnrolledTerm(term: Int) {
        _enrolledTerm.postValue(term)
    }

    fun setCurriculum(curriculum: String) {
        _selectedCurriculum.postValue(curriculum)
    }

    //ADMIN Curriculum Overview functions
    fun fetchStudentData(studentId: String) {
        val studentRef = repository.getStudentDocument(studentId)

        Log.d("CurriculumViewModel", "Fetching Firestore document for student: $studentId")

        studentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Log.d("CurriculumViewModel", "Student document found: $document")

                val completed = document.get("completedCourses") as? List<String> ?: emptyList()
                _completedCourses.postValue(completed.toSet())

                val enrolledTerm = document.getLong("enrolledTerm")?.toInt() ?: 1
                _enrolledTerm.postValue(enrolledTerm)

                // ✅ FIX: Use "curriculum" instead of "curriculumId" and map it correctly
                val curriculumId = document.getString("curriculum")?.let { id ->
                    when (id) {
                        "1" -> "bscpe_2022_2023" // ✅ Map curriculum ID 1 to actual Firestore ID
                        else -> null
                    }
                }

                if (curriculumId != null) {
                    fetchCurriculum(curriculumId)  // Fetch courseGraph from Firestore
                } else {
                    Log.e("CurriculumViewModel", "curriculumId is null for student: $studentId")
                }
            } else {
                Log.e("CurriculumViewModel", "Student document does not exist!")
            }
        }.addOnFailureListener { e ->
            Log.e("CurriculumViewModel", "Error fetching student document", e)
        }

        repository.getStudentEmail(
            studentId,
            onSuccess = { email ->
                _studentEmail.value = email
                Log.d("CurriculumViewModel", "Fetched student email: $email")
            },
            onFailure = { exception ->
                _studentEmail.value = "Error Fetching Email"
                Log.e("CurriculumViewModel", "Failed to fetch student email", exception)
            }
        )
    }

    fun fetchCurriculum(curriculumId: String) {
        val curriculumRef = repository.getCurriculumDocument(curriculumId)

        Log.d("CurriculumViewModel", "Fetching all courses for curriculum: $curriculumId")

        val groupedCourses = mutableMapOf<Int, MutableMap<Int, MutableList<CourseNode>>>()
        val electiveCourses = mutableListOf<CourseNode>()

        val years = listOf("1", "2", "3", "4") // 🔹 Years inferred from Firestore path
        val terms = listOf("term_1", "term_2", "term_3") // 🔹 Terms inferred from Firestore path
        val electiveCategories = listOf("AWS171P", "EMSY171P", "GEN_ED", "MACH171P", "MICR172P", "NETA172P", "SDEV173P", "SNAD174P")

        val tasks = mutableListOf<Task<QuerySnapshot>>()

        // 🔹 Fetch all regular courses (Loop through all years and terms)
        for (year in years) {
            for (term in terms) {
                val termPath = curriculumRef.collection(year).document(term).collection("courses")

                val task = termPath.get().addOnSuccessListener { snapshot ->
                    val courses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(CourseNode::class.java)?.copy(
                            regularTerms = listOf(term.removePrefix("term_").toInt()) // ✅ Set term correctly
                        )
                    }

                    if (courses.isNotEmpty()) {
                        groupedCourses.getOrPut(year.toInt()) { mutableMapOf() }
                            .getOrPut(term.removePrefix("term_").toInt()) { mutableListOf() }
                            .addAll(courses)
                    }
                }.addOnFailureListener { e ->
                    Log.e("CurriculumViewModel", "Error fetching courses for Year $year - Term $term", e)
                }
                tasks.add(task)
            }
        }

        // 🔹 Fetch all electives (Loop through elective categories)
        for (category in electiveCategories) {
            val electivePath = curriculumRef.collection("electives").document(category).collection("courses")

            val electiveTask = electivePath.get().addOnSuccessListener { snapshot ->
                val electives = snapshot.documents.mapNotNull { it.toObject(CourseNode::class.java) }
                electiveCourses.addAll(electives)
            }.addOnFailureListener { e ->
                Log.e("CurriculumViewModel", "Error fetching electives for $category", e)
            }
            tasks.add(electiveTask)
        }

        // 🔹 Wait for all tasks to complete before updating the UI
        Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
            if (groupedCourses.isNotEmpty() || electiveCourses.isNotEmpty()) {
                Log.d("CurriculumViewModel", "Total courses fetched (including electives): ${groupedCourses.size}")

                _courseGraph.postValue(
                    CourseGraph(
                        groupedCourses = groupedCourses.mapValues { (_, terms) ->
                            terms.mapValues { (_, courses) -> courses.toList() }
                        },
                        electives = electiveCourses
                    )
                )

            } else {
                Log.e("CurriculumViewModel", "No courses found in curriculum: $curriculumId")
            }
        }.addOnFailureListener { e ->
            Log.e("CurriculumViewModel", "Error fetching curriculum data", e)
        }
    }

    fun toggleCourseCompletion(studentId: String, courseCode: String) {
        val updatedCourses = _completedCourses.value?.toMutableSet() ?: mutableSetOf()

        if (updatedCourses.contains(courseCode)) {
            updatedCourses.remove(courseCode)  // Uncheck course
            Log.d("CurriculumViewModel", "Course $courseCode unchecked")
        } else {
            updatedCourses.add(courseCode)  // Mark as completed
            Log.d("CurriculumViewModel", "Course $courseCode checked")
        }

        _completedCourses.postValue(updatedCourses)

        // Update Firestore
        val studentRef = repository.getStudentDocument(studentId)
        studentRef.update("completedCourses", updatedCourses.toList())
        Log.d("CurriculumViewModel", "Updated completedCourses for student: $studentId")
    }

    fun getAvailableCourses(selectedTerm: Int): List<Pair<CourseNode, String>> {
        Log.d("CurriculumViewModel", "Starting getAvailableCourses() for Term $selectedTerm")

        val graph = _courseGraph.value
        if (graph == null) {
            Log.e("CurriculumViewModel", "Course graph is null, returning empty list")
            return emptyList()
        }

        Log.d("CurriculumViewModel", "Course graph successfully retrieved")

        val completed = _completedCourses.value ?: emptySet()
        Log.d("CurriculumViewModel", "Student's Completed Courses: $completed")

        Log.d("CurriculumViewModel", "Fetching next available courses for Term $selectedTerm...")
        val availableCourses = graph.getNextAvailableCourses(selectedTerm, completed)

        Log.d(
            "CurriculumViewModel",
            "Available Courses for Term $selectedTerm: ${availableCourses.map { it.first.code }}"
        )

        return availableCourses
    }


    // functions to upload BS CPE 2022-2023 CURRICULUM
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
    fun uploadFirstYearTerm3() {
        val firstYearTerm3Courses = listOf(
            mapOf(
                "code" to "CPE141L",
                "name" to "Programming Logic and Design (Lab)",
                "units" to 2,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf("CPE001L")
            ),
            mapOf(
                "code" to "DRAW023L-1",
                "name" to "Computer-Aided Drafting (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("DRAW021W"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "HUM039",
                "name" to "Ethics",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH042",
                "name" to "Engineering Calculus 2",
                "units" to 4,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PHY035",
                "name" to "Physics for Engineers",
                "units" to 4,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PHY035L",
                "name" to "Physics for Engineers (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf("PHY035")
            ),
            mapOf(
                "code" to "VE023",
                "name" to "Life Coaching Series 3",
                "units" to -1,
                "prerequisites" to listOf("VE022"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 1, "term_3", firstYearTerm3Courses){
            // Handle completion
            Log.d("UploadDebug", "First Year Term 3 courses uploaded")
        }
    }

    fun uploadSecondYearTerm1() {
        val secondYearTerm1Courses = listOf(
            mapOf(
                "code" to "CPE105-1",
                "name" to "Discrete Mathematics",
                "units" to 3,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE142L",
                "name" to "Object Oriented Programming (Lab)",
                "units" to 2,
                "prerequisites" to listOf("CPE001L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "ECE121L",
                "name" to "Computer-Aided Calculations (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "EE101-3",
                "name" to "Fundamentals of Electrical Circuits",
                "units" to 3,
                "prerequisites" to listOf("MATH042", "PHY035"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "EE101L-3",
                "name" to "Fundamentals of Electrical Circuits (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("PHY035L"),
                "coRequisites" to listOf("EE101-3")
            ),
            mapOf(
                "code" to "IE101-1",
                "name" to "Engineering Data Analysis",
                "units" to 3,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH056",
                "name" to "Differential Equations",
                "units" to 3,
                "prerequisites" to listOf("MATH042"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PE001",
                "name" to "Physical Activities Toward Health and Fitness 1",
                "units" to 2,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 2, "term_1", secondYearTerm1Courses){
            // Handle completion
            println("Second Year Term 1 courses uploaded")
        }
    }
    fun uploadSecondYearTerm2() {
        val secondYearTerm2Courses = listOf(
            mapOf(
                "code" to "ECE101-3",
                "name" to "Fundamental of Electronic Circuits",
                "units" to 3,
                "prerequisites" to listOf("EE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "ECE101L-3",
                "name" to "Fundamental of Electronic Circuits (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("EE101-3"),
                "coRequisites" to listOf("ECE101-3")
            ),
            mapOf(
                "code" to "EECO102",
                "name" to "Engineering Economy",
                "units" to 3,
                "prerequisites" to listOf("IE101-1"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "ENG041",
                "name" to "Purposive Communication",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "GEELEC01",
                "name" to "GE Elective 1",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH116",
                "name" to "Advanced Engineering Mathematics",
                "units" to 3,
                "prerequisites" to listOf("MATH056"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH116E",
                "name" to "Engineering Mathematics Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("IE101-1", "MATH056"),
                "coRequisites" to listOf("MATH116")
            ),
            mapOf(
                "code" to "SS023",
                "name" to "The Contemporary World",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PE002",
                "name" to "Physical Activities Toward Health and Fitness 2",
                "units" to 2,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 2, "term_2", secondYearTerm2Courses){
            // Handle completion
            println("Second Year Term 2 courses uploaded")
        }
    }
    fun uploadSecondYearTerm3() {
        val secondYearTerm3Courses = listOf(
            mapOf(
                "code" to "CPE101-1",
                "name" to "Digital Electronics: Logic Circuits and Design",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE101L-1",
                "name" to "Digital Electronics: Logic Circuits and Design (Lab)",
                "units" to 1,
                "prerequisites" to listOf("ECE101L-3"),
                "coRequisites" to listOf("CPE101-1")
            ),
            mapOf(
                "code" to "CPE106-1",
                "name" to "Data and Digital Communications",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE121L-1",
                "name" to "Computer Engineering Drafting and Design (Lab)",
                "units" to 1,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CS105L",
                "name" to "Data Structures and Algorithms (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE141L", "CPE142L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH161",
                "name" to "Numerical Methods",
                "units" to 3,
                "prerequisites" to listOf("MATH116"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "MATH161L",
                "name" to "Numerical Methods (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("MATH116", "ECE121L"),
                "coRequisites" to listOf("MATH161")
            ),
            mapOf(
                "code" to "EMGT100",
                "name" to "Engineering Management",
                "units" to 2,
                "prerequisites" to listOf("EECO102"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SS038",
                "name" to "The Life and Works of Jose Rizal",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PE003",
                "name" to "Physical Activities Toward Health and Fitness 3",
                "units" to 2,
                "prerequisites" to listOf("PE001", "PE002"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 2, "term_3", secondYearTerm3Courses){
            // Handle completion
            println("Second Year Term 3 courses uploaded")
        }
    }

    fun uploadThirdYearTerm1() {
        val thirdYearTerm1Courses = listOf(
            mapOf(
                "code" to "CPE103-4",
                "name" to "Microprocessors",
                "units" to 3,
                "prerequisites" to listOf("CPE101-1"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE103L-4",
                "name" to "Microprocessors (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE101L-1"),
                "coRequisites" to listOf("CPE103-4")
            ),
            mapOf(
                "code" to "CPE107-1",
                "name" to "Software Design",
                "units" to 3,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE107L-1",
                "name" to "Software Design (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf("CPE107-1")
            ),
            mapOf(
                "code" to "CPE143L",
                "name" to "Web Design and Development (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE142L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "ECE130",
                "name" to "Feedback and Control Systems",
                "units" to 3,
                "prerequisites" to listOf("MATH116"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "GEELEC02",
                "name" to "GE Elective 2",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "TEC100",
                "name" to "Technopreneurship",
                "units" to 3,
                "prerequisites" to listOf("EMGT100"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "PE004",
                "name" to "Physical Activities Toward Health and Fitness 4",
                "units" to 2,
                "prerequisites" to listOf("PE001", "PE002"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 3, "term_1", thirdYearTerm1Courses){
            // Handle completion
            println("Third Year Term 1 courses uploaded")
        }
    }
    fun uploadThirdYearTerm2() {
        val thirdYearTerm2Courses = listOf(
            mapOf(
                "code" to "CPE104L-1",
                "name" to "Introduction to Hardware Description Language (Lab)",
                "units" to 1,
                "prerequisites" to listOf("CPE141L", "ECE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE108E",
                "name" to "General Engineering and Applied Sciences Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CHM031", "PHY035", "EECO102"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE108P",
                "name" to "Fundamentals of Mixed Signals and Sensors",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE109-1",
                "name" to "Computer Networks and Security",
                "units" to 3,
                "prerequisites" to listOf("CPE106-1"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE109L-1",
                "name" to "Computer Networks and Security (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE106-1"),
                "coRequisites" to listOf("CPE109-1")
            ),
            mapOf(
                "code" to "CPE144L",
                "name" to "Mobile Application Development (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE142L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "EENV102",
                "name" to "Environmental Science and Engineering",
                "units" to 3,
                "prerequisites" to listOf("CHM031"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "NETA172P-1",
                "name" to "CCNA Routing and Switching 1 (Paired)",
                "units" to 3,
                "prerequisites" to listOf("PHY035"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "RES101",
                "name" to "Methods of Research",
                "units" to 3,
                "prerequisites" to listOf("IE101-1", "ENG024"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 3, "term_2", thirdYearTerm2Courses){
            // Handle completion
            println("Third Year Term 2 courses uploaded")
        }
    }
    fun uploadThirdYearTerm3() {
        val thirdYearTerm3Courses = listOf(
            mapOf(
                "code" to "CAP200D",
                "name" to "Capstone Design / Thesis 1 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("RES101"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE112-1",
                "name" to "Embedded Systems",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE112L-1",
                "name" to "Embedded Systems (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE103L-4"),
                "coRequisites" to listOf("CPE112-1")
            ),
            mapOf(
                "code" to "CPE112-1E",
                "name" to "Computer Engineering 2 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE103-4", "CPE104L-1", "CPE108P", "EE101-3"),
                "coRequisites" to listOf("CPE112-1")
            ),
            mapOf(
                "code" to "CPE144E",
                "name" to "Computer Engineering 1 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE142L", "CPE143L", "CS105L", "CPE107-1"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE151",
                "name" to "Operating Systems",
                "units" to 3,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE151L",
                "name" to "Operating Systems (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf("CPE151")
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 3, "term_3", thirdYearTerm3Courses){
            // Handle completion
            println("Third Year Term 3 courses uploaded")
        }
    }
    fun uploadFourthYearTerm1() {
        val fourthYearTerm1Courses = listOf(
            mapOf(
                "code" to "CAP200D-1",
                "name" to "Capstone Design / Thesis 2 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CAP200D"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE110-1",
                "name" to "Emerging Technologies in CPE",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE113-1",
                "name" to "Digital Signal Processing",
                "units" to 3,
                "prerequisites" to listOf("ECE130"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE113L-1",
                "name" to "Digital Signal Processing (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("ECE130", "ECE121L"),
                "coRequisites" to listOf("CPE113-1")
            ),
            mapOf(
                "code" to "CPEELEC02",
                "name" to "CPE Elective 2 (Paired)",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SS085",
                "name" to "Philippine Indigenous Communities",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "COR198",
                "name" to "Correlation 1",
                "units" to -1,  // (1.0) but no actual credit, stored as -1 for distinction
                "prerequisites" to listOf("CHM031", "MATH116", "MATH116E", "PHY035", "EECO102"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 4, "term_1", fourthYearTerm1Courses){
            // Handle completion
            println("Fourth Year Term 1 courses uploaded")
        }
    }
    fun uploadFourthYearTerm2() {
        val fourthYearTerm2Courses = listOf(
            mapOf(
                "code" to "CAP200D-2",
                "name" to "Capstone Design / Thesis 3 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CAP200D"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE131-1",
                "name" to "Computer Architecture and Organization",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE131L-1",
                "name" to "Computer Architecture and Organization (Lab)",
                "units" to 1,
                "prerequisites" to listOf("CPE103L-4"),
                "coRequisites" to listOf("CPE131-1")
            ),
            mapOf(
                "code" to "CPE181",
                "name" to "CPE Laws and Professional Practice",
                "units" to 2,
                "prerequisites" to listOf("HUM039"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE181E",
                "name" to "Computer Engineering 3 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE109-1", "CPE110-1", "CPE113-1", "CPE151"),
                "coRequisites" to listOf("CPE131-1", "CPE181")
            ),
            mapOf(
                "code" to "CPEELEC03",
                "name" to "CPE Elective 3 (Paired)",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE198-2",
                "name" to "CPE Correlation 2",
                "units" to -1,  // (1.0) but no actual credit, stored as -1 for distinction
                "prerequisites" to listOf("CPE112-1", "CPE113-1", "CPE151", "CPE108E", "CPE144E", "CPE112-1E"),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 4, "term_2", fourthYearTerm2Courses){
            // Handle completion
            println("Fourth Year Term 2 courses uploaded")
        }
    }
    fun uploadFourthYearTerm3() {
        val fourthYearTerm3Courses = listOf(
            mapOf(
                "code" to "CPE191F-1",
                "name" to "CPE Seminars and Field Trips (Field)",
                "units" to 1,
                "prerequisites" to listOf("CPE109-1", "CPE144L"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "CPE199R-1",
                "name" to "CPE Practicum",
                "units" to 3,
                "prerequisites" to listOf("CPE143L", "CS105L", "ECE101-3", "HUM039", "CAP200D-2"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SAF102",
                "name" to "Basic Occupational Safety and Health",
                "units" to 3,
                "prerequisites" to listOf("CHM031"),
                "coRequisites" to listOf<String>()
            ),
            mapOf(
                "code" to "SGE101",
                "name" to "Student Global Experience",
                "units" to 0,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>()
            )
        )

        repository.uploadCourses("bscpe_2022_2023", 4, "term_3", fourthYearTerm3Courses){
            // Handle completion
            println("Fourth Year Term 3 courses uploaded")
        }
    }
    fun uploadElectives() {
        val electives = mapOf(
            "NETA172P" to listOf(
                mapOf("code" to "NETA172P-2", "name" to "CCNA Routing and Switching 2 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "NETA172P-3", "name" to "CCNA Routing and Switching 3 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>()),
                mapOf("code" to "NETA172P-4", "name" to "CCNA Routing and Switching 4 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>())
            ),
            "MICR172P" to listOf(
                mapOf("code" to "MICR172P-1", "name" to "Microelectronics 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE103-4", "CPE104L-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "MICR172P-2", "name" to "Microelectronics 2 (Paired)", "units" to 3, "prerequisites" to listOf("MICR172P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "MICR172P-3", "name" to "Microelectronics 3 (Paired)", "units" to 3, "prerequisites" to listOf("MICR172P-2"), "coRequisites" to listOf<String>())
            ),
            "MACH171P" to listOf(
                mapOf("code" to "MACH171P-1", "name" to "Machine Learning 1 (Paired)", "units" to 3, "prerequisites" to listOf("MATH116", "CS105L"), "coRequisites" to listOf<String>()),
                mapOf("code" to "MACH171P-2", "name" to "Machine Learning 2 (Paired)", "units" to 3, "prerequisites" to listOf("MACH171P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "MACH171P-3", "name" to "Machine Learning 3 (Paired)", "units" to 3, "prerequisites" to listOf("MACH171P-2"), "coRequisites" to listOf<String>())
            ),
            "EMSY171P" to listOf(
                mapOf("code" to "EMSY171P-1", "name" to "Embedded Systems 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE103-4", "CPE104L-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "EMSY171P-2", "name" to "Embedded Systems 2 (Paired)", "units" to 3, "prerequisites" to listOf("EMSY171P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "EMSY171P-3", "name" to "Embedded Systems 3 (Paired)", "units" to 3, "prerequisites" to listOf("EMSY171P-2"), "coRequisites" to listOf<String>())
            ),
            "SDEV173P" to listOf(
                mapOf("code" to "SDEV173P-1", "name" to "Software Development 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE107-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "SDEV173P-2", "name" to "Software Development 2 (Paired)", "units" to 3, "prerequisites" to listOf("SDEV173P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "SDEV173P-3", "name" to "Software Development 3 (Paired)", "units" to 3, "prerequisites" to listOf("SDEV173P-2"), "coRequisites" to listOf<String>())
            ),
            "SNAD174P" to listOf(
                mapOf("code" to "SNAD174P-1", "name" to "System and Network Administration 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE109-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "SNAD174P-2", "name" to "System and Network Administration 2 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD174P-1"), "coRequisites" to listOf<String>()),
                mapOf("code" to "SNAD174P-3", "name" to "System and Network Administration 3 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD174P-2"), "coRequisites" to listOf<String>())
            ),
            "AWS171P" to listOf(
                mapOf("code" to "AWS171P", "name" to "Cloud Foundations (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "AWS171P-1", "name" to "Cloud Development (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>()),
                mapOf("code" to "AWS171P-2", "name" to "Cloud Architecture (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>()),
                mapOf("code" to "AWS171P-3", "name" to "Cloud System Operations (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>())
            ),
            "GEN_ED" to listOf(
                mapOf("code" to "ENV075", "name" to "Environmental Science", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "ENV076", "name" to "People and the Earth's Ecosystem", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "ENV078", "name" to "The Entrepreneurial Mind", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "HUM080", "name" to "Philippine Popular Culture", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "HUM081", "name" to "Indigenous Creative Crafts", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "HUM082", "name" to "Reading Visual Art", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "HUM083", "name" to "Great Books", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "SS084", "name" to "Religions, Religious Experiences and Spirituality", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>()),
                mapOf("code" to "SS086", "name" to "Gender and Society", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>())
            )
        )

        repository.uploadElectives("bscpe_2022_2023", electives)
    }
    fun updateRegularTermsInFirestore() {
        repository.updateCoursesWithRegularTerms("bscpe_2022_2023")
    }
    fun updateRegularTermsForElectives() {
        repository.updateElectivesWithRegularTerms("bscpe_2022_2023")
    }























    // Duff added feb - 15
    fun updateCurriculumInFirestore(selectedCurriculum: String, selectedTerm: Int, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("students").document(userId)
            .update(
                "curriculum", selectedCurriculum,
                "termEnrolling", selectedTerm,
                "approvalStatus", "pending"
            )
            .addOnSuccessListener {
                onSuccess() // Calls navigation logic after update
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update curriculum: ${e.message}")
            }
    }
    // duff added feb 22
    private val _studentProgram = MutableLiveData<String>()
    val studentProgram: LiveData<String> get() = _studentProgram

    init {
        getStudentProgramFromFirestore()
    }
    private fun getStudentProgramFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("students").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _studentProgram.value = document.getString("program") ?: ""
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to fetch student program: ${e.message}")
                }
        }
    }
    // ----------------------------------









}

