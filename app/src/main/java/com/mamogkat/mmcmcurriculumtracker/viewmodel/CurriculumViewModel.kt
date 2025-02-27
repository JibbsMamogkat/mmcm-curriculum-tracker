package com.mamogkat.mmcmcurriculumtracker.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mamogkat.mmcmcurriculumtracker.models.CourseGraph
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

class CurriculumViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _courseGraph = MutableLiveData<CourseGraph>()
    val courseGraph: LiveData<CourseGraph> = _courseGraph

    private val _completedCourses = MutableLiveData<Set<String>>()
    val completedCourses: LiveData<Set<String>> = _completedCourses

    private val _studentCompletedCourses = SnapshotStateMap<String, Set<String>>()
    val studentCompletedCourses: SnapshotStateMap<String, Set<String>> = _studentCompletedCourses

    private val _enrolledTerm = MutableLiveData<Int>()
    val enrolledTerm: LiveData<Int> = _enrolledTerm

    private val _selectedCurriculum = MutableLiveData<String>()
    val selectedCurriculum: LiveData<String> = _selectedCurriculum

    private val _studentEmail = MutableLiveData<String>()
    val studentEmail: LiveData<String> get() = _studentEmail

    private val _studentApprovalStatus = MutableLiveData<String>()
    val studentApprovalStatus: LiveData<String> get() = _studentApprovalStatus


    fun setEnrolledTerm(term: Int) {
        _enrolledTerm.postValue(term)
    }

    fun setCurriculum(curriculum: String) {
        _selectedCurriculum.postValue(curriculum)
    }
    private val _curriculumName = MutableLiveData<String>()
    val curriculumName: LiveData<String> = _curriculumName

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
                        "2" -> "bsee_2024_2025"
                        "3" -> "bscpe_2021_2022"
                        "4" -> "bsece_2022_2023"
                        else -> null
                    }
                }

                if (curriculumId != null) {
                    fetchCurriculum(curriculumId)  // Fetch courseGraph from Firestore
                    // 🔹 Fetch the curriculum name from Firestore
                    repository.getCurriculumDocument(curriculumId)
                        .get()
                        .addOnSuccessListener { curriculumDoc ->
                            val curriculumName = curriculumDoc.getString("name") ?: "Unknown Curriculum"
                            _curriculumName.postValue(curriculumName) // Store it in LiveData
                        }
                        .addOnFailureListener { e ->
                            Log.e("CurriculumViewModel", "Error fetching curriculum name", e)
                            _curriculumName.postValue("Error Fetching Curriculum")
                        }
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

        // 🔹 Elective categories to fetch depending on the curriculum
        val electiveCategories = mutableListOf<String>()
        Log.d("ElectivesBUG", "Fetching electives for curriculum: $curriculumId")
        if (curriculumId == "bscpe_2022_2023") {
            electiveCategories.addAll(
                listOf(
                    "AWS171P",
                    "EMSY171P",
                    "GEN_ED",
                    "MACH171P",
                    "MICR172P",
                    "NETA172P",
                    "SDEV173P",
                    "SNAD174P"
                )
            )
        } else if (curriculumId == "bsee_2024_2025") {
            electiveCategories.addAll(
                listOf(
                    "ADVANCED_ELECTRICAL_SYSTEMS_DESIGN",
                    "ADVANCED_POWER_SYSTEMS",
                    "ADVANCED_SYSTEM_DESIGN",
                    "AGRICULTURAL_ENGINEERING",
                    "GEN_ED",
                    "MECHATRONICS",
                    "OPEN_ELECTIVE"
                )
            )
        }
        else if (curriculumId == "bscpe_2021_2022") {
            electiveCategories.addAll(
                listOf(
                    "AWS171P",
                    "EMSY171P",
                    "GEN_ED",
                    "MACH171P",
                    "MICR172P",
                    "NETA172P",
                    "SDEV173P",
                    "SNAD174P"
                )
            )
        }
        else if(curriculumId == "bsece_2022_2023") {
            electiveCategories.addAll(
                listOf(
                    "ECE137P",
                    "ECE110P",
                    "ECE154P",
                    "ECE152P",
                    "SNAD175P",
                    "NETA172P",
                    "AWS171P",
                    "ECE194",
                    "NETA173P",
                    "ECE166P",
                    "ECE153",
                    "ECE118P",
                    "ECE127",
                    "AENG",
                    "GEN_ED"
                )
            )
        }
        Log.d("ElectivesBUG", "Elective categories to fetch: $electiveCategories")
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
                Log.d("ElectivesBUG", "Electives for category: $category fetched: $electives")
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

    fun updateCompletedCourses(studentId: String, courseCode: String, isChecked: Boolean) {
        val updatedCourses = _completedCourses.value?.toMutableSet() ?: mutableSetOf()

        if (isChecked) {
            updatedCourses.add(courseCode)
        } else {
            updatedCourses.remove(courseCode)
        }

        _completedCourses.value = updatedCourses // ✅ Update LiveData

        repository.updateCompletedCourses(studentId, updatedCourses) // ✅ Delegate Firestore update
    }



   //Admin Next Courses Available functions
    fun loadStudentCompletedCourses(studentId: String, onComplete: () -> Unit) {

        viewModelScope.launch {
            Log.d("CurriculumViewModel", "Loading completed courses for Student ID: $studentId")

            val completedCourses = repository.getStudentCompletedCourses(studentId) // Fetch Firestore data

            _studentCompletedCourses[studentId] = completedCourses

            Log.d("CurriculumViewModel", "Completed courses for Student $studentId: $completedCourses")

            // 🔹 Now that completed courses are loaded, trigger available courses update
            onComplete()
        }
    }

    private val _availableCourses = MutableStateFlow<List<Pair<CourseNode, String>>>(emptyList())
    val availableCourses: StateFlow<List<Pair<CourseNode, String>>> = _availableCourses


   fun getAvailableCourses(studentId: String, selectedTerm: Int) {
        Log.d("CurriculumViewModel", "Starting computeAvailableCourses() for Student ID: $studentId, Term: $selectedTerm")

        viewModelScope.launch {
            val graph = _courseGraph.value
            if (graph == null) {
                Log.e("CurriculumViewModel", "Course graph is null, returning empty list")
                _availableCourses.value = emptyList()
                return@launch
            }

            Log.d("CurriculumViewModel", "Course graph successfully retrieved")

            val completed = _studentCompletedCourses[studentId] ?: emptySet()
            Log.d("CurriculumViewModel", "Student $studentId's Completed Courses: $completed")

            Log.d("CurriculumViewModel", "Fetching next available courses for Term $selectedTerm...")
            _availableCourses.value = graph.getNextAvailableCourses(selectedTerm, completed)
        }
    }
    // Duff added for bugs
    // CurriculumViewModel.kt
    private val _studentCurriculum = mutableMapOf<String, String>()
    fun observeStudentData(studentId: String, onComplete: () -> Unit) {
        Log.d("CurriculumViewModel", "Starting real-time listener for Student ID: $studentId")

        repository.listenToStudentData(studentId) { completedCourses, curriculum ->  // ✅ Two parameters
            viewModelScope.launch {
                Log.d("CurriculumViewModel", "Real-time update: Completed - $completedCourses | Curriculum - $curriculum")

                _studentCompletedCourses[studentId] = completedCourses
                _studentCurriculum[studentId] = curriculum ?: ""  // ✅ Store curriculum

                getAvailableCoursesStudent(studentId, 1)  // ✅ Trigger update
                onComplete() // ✅ Call completion callback
            }
        }
    }


    fun getAvailableCoursesStudent(studentId: String, selectedTerm: Int) {
        Log.d("CurriculumViewModel", "Starting computeAvailableCourses() for Student ID: $studentId, Term: $selectedTerm")

        viewModelScope.launch {
            _courseGraph.asFlow().filterNotNull().firstOrNull()?.let { graph ->
                Log.d("CurriculumViewModel", "Course graph successfully retrieved")

                val completed = _studentCompletedCourses[studentId] ?: emptySet()
                Log.d("CurriculumViewModel", "Student $studentId's Completed Courses: $completed")

                Log.d("CurriculumViewModel", "Fetching next available courses for Term $selectedTerm...")
                _availableCourses.value = graph.getNextAvailableCourses(selectedTerm, completed)
            } ?: Log.e("CurriculumViewModel", "Course graph is null even after waiting, returning empty list")
        }
    }
    // for UI Curriculum student
    private val _expandedYears = MutableStateFlow(mapOf<Int, Boolean>())
    val expandedYears: StateFlow<Map<Int, Boolean>> = _expandedYears

    private val _expandedTerms = MutableStateFlow(mapOf<Pair<Int, Int>, Boolean>())
    val expandedTerms: StateFlow<Map<Pair<Int, Int>, Boolean>> = _expandedTerms

    private val _expandedElectives = MutableStateFlow(false)
    val expandedElectives: StateFlow<Boolean> = _expandedElectives

    private val _scrollPosition = MutableStateFlow(0)
    val scrollPosition: StateFlow<Int> = _scrollPosition

    private val _scrollOffset = MutableStateFlow(0)
    val scrollOffset: StateFlow<Int> = _scrollOffset

    fun toggleYearExpansion(year: Int) {
        _expandedYears.value = _expandedYears.value.toMutableMap().apply {
            this[year] = !(this[year] ?: false)
        }
    }

    fun toggleTermExpansion(year: Int, term: Int, allCompleted: Boolean) {
        _expandedTerms.value = _expandedTerms.value.toMutableMap().toMap().let { currentMap ->
            val newState = !currentMap.getOrDefault(Pair(year, term), !allCompleted) // Correct default state
            currentMap.toMutableMap().apply { this[Pair(year, term)] = newState }
        }
    }


    fun toggleElectivesExpansion() {
        _expandedElectives.value = !_expandedElectives.value
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _scrollPosition.value = index
        _scrollOffset.value = offset
    }
    //----------------------------------------------

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
    fun updateYearLevelandTermInFirestore() {
//        repository.updateCoursesWithYearLevelAndTerm("bscpe_2022_2023")
        repository.updateElectivesWithYearLevelAndTerm("bscpe_2022_2023")
    }

 // uploading BS EE 2024-2025
 fun uploadBSEE_2024_2025() {
     val firstYearTerm1Courses = listOf(
         mapOf(
             "code" to "CHM031",
             "name" to "Chemistry for Engineers",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "CHM031L",
             "name" to "Chemistry for Engineers (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf("CHM031"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "EE100L",
             "name" to "Electrical Engineering Orientation (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "ENG023",
             "name" to "Receptive Communication Skills",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "HUM021",
             "name" to "Logic and Critical Thinking",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "MATH030",
             "name" to "College Algebra",
             "units" to 2,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "MATH032",
             "name" to "Precalculus",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "NSTP010",
             "name" to "National Service Training Program 1",
             "units" to -3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "VE021",
             "name" to "Life Coaching Series 1",
             "units" to -1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 1
         )
     )

     val firstYearTerm2Courses = listOf(
         mapOf(
             "code" to "DRAW021W",
             "name" to "Engineering Drawing 1",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "ENG024",
             "name" to "Writing for Academic Studies",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "HUM039",
             "name" to "Ethics",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "MATH035",
             "name" to "Mathematics in the Modern World",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "MATH041",
             "name" to "Engineering Calculus 1",
             "units" to 4,
             "prerequisites" to listOf("MATH030", "MATH032"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "SS021",
             "name" to "Understanding the Self",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "NSTP011P",
             "name" to "National Service Training Program 2 (Paired)",
             "units" to -3,
             "prerequisites" to listOf("NSTP010"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "VE022",
             "name" to "Life Coaching Series 2",
             "units" to -1,
             "prerequisites" to listOf("VE021"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 1
         )

     )

     val firstYearTerm3Courses = listOf(
         mapOf(
             "code" to "CPE001L",
             "name" to "Computer Fundamentals and Programming 1 (Lab)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "DRAW023L-1",
             "name" to "Computer-Aided Drafting (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("DRAW021W"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "EE099L",
             "name" to "Basic Electricity and Electronics Workshop (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "ENG041",
             "name" to "Purposive Communication",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "IE101-1",
             "name" to "Engineering Data Analysis",
             "units" to 3,
             "prerequisites" to listOf("MATH041"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "MATH042",
             "name" to "Engineering Calculus 2",
             "units" to 4,
             "prerequisites" to listOf("MATH041"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "PE011",
             "name" to "Physical Activities Toward Health and Fitness 1",
             "units" to 2,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "PHY035",
             "name" to "Physics for Engineers",
             "units" to 4,
             "prerequisites" to listOf("MATH041"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "PHY035L",
             "name" to "Physics for Engineers (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("MATH041"),
             "coRequisites" to listOf("PHY035"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         ),
         mapOf(
             "code" to "VE023",
             "name" to "Life Coaching Series 3",
             "units" to -1,
             "prerequisites" to listOf("VE022"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 1
         )
     )

     val secondYearTerm1Courses = listOf(
         mapOf(
             "code" to "DS100L",
             "name" to "Applied Data Science Laboratory",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "ECE121L",
             "name" to "Computer-Aided Calculations (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "ECE0102",
             "name" to "Engineering Economy",
             "units" to 3,
             "prerequisites" to listOf("IE101-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE100L-1",
             "name" to "Building Wiring Installation Technology (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE100L"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE101-2",
             "name" to "Circuits 1",
             "units" to 3,
             "prerequisites" to listOf("MATH042", "PHY035"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE101L-2",
             "name" to "Circuits 1 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("PHY035L"),
             "coRequisites" to listOf("EE101-2"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MATH056",
             "name" to "Differential Equations",
             "units" to 3,
             "prerequisites" to listOf("MATH042"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MET111",
             "name" to "Thermodynamics",
             "units" to 3,
             "prerequisites" to listOf("MATH042", "PHY035"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "NET100-1",
             "name" to "Engineering Mechanics",
             "units" to 3,
             "prerequisites" to listOf("MATH042", "PHY035"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "PE012",
             "name" to "Physical Activities Toward Health and Fitness 2",
             "units" to 2,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 2
         )
     )
     val secondYearTerm2Courses = listOf(
         mapOf(
             "code" to "ECE101-4",
             "name" to "Electronics Circuits: Devices and Analysis",
             "units" to 3,
             "prerequisites" to listOf("EE101-2"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "ECE101L-4",
             "name" to "Electronics Circuits: Devices and Analysis (Lab)",
             "units" to 1,
             "prerequisites" to listOf("EE101L-2"),
             "coRequisites" to listOf("ECE101-4"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "ECE115-1",
             "name" to "Electromagnetics for EE",
             "units" to 2,
             "prerequisites" to listOf("PHY035", "MATH056"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE102-1",
             "name" to "Circuits 2",
             "units" to 3,
             "prerequisites" to listOf("EE101-2"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE102L-1",
             "name" to "Circuits 2 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE101L-2"),
             "coRequisites" to listOf("EE102-1"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EMGT100",
             "name" to "Engineering Management",
             "units" to 2,
             "prerequisites" to listOf("ECE0102"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EMGT100L",
             "name" to "Project Management (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("ECE0102"),
             "coRequisites" to listOf("EMGT100"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EENV102",
             "name" to "Environmental Science and Engineering",
             "units" to 3,
             "prerequisites" to listOf("CHM031"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MATH116",
             "name" to "Advanced Engineering Mathematics",
             "units" to 3,
             "prerequisites" to listOf("MATH056"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "PE013",
             "name" to "Physical Activities Toward Health and Fitness 3",
             "units" to 2,
             "prerequisites" to listOf("PE011", "PE012"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 2
         )
     )

     val secondYearTerm3Courses = listOf(
         mapOf(
             "code" to "CE104-2",
             "name" to "Mechanics of Deformable Bodies for EE",
             "units" to 3,
             "prerequisites" to listOf("MEC100-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE103",
             "name" to "Circuits 3",
             "units" to 2,
             "prerequisites" to listOf("EE102-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE103L",
             "name" to "Circuits 3 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE102L-1"),
             "coRequisites" to listOf("EE103"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE106-1",
             "name" to "DC Machinery",
             "units" to 2,
             "prerequisites" to listOf("EE102-1", "ECE115-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE106L-1",
             "name" to "DC Machinery (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE102L-1"),
             "coRequisites" to listOf("EE106-1"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE100L-2",
             "name" to "Industrial Motor Control Technology (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "EE181",
             "name" to "Electrical Engineering Laws, Codes, and Ethics",
             "units" to 2,
             "prerequisites" to listOf("HUM039"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MATH161",
             "name" to "Numerical Methods",
             "units" to 3,
             "prerequisites" to listOf("MATH116"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MATH161L",
             "name" to "Numerical Methods (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("MATH116", "ECE121L"),
             "coRequisites" to listOf("MATH161"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "MATH800E",
             "name" to "Engineering Mathematics Exit Exam",
             "units" to 0,
             "prerequisites" to listOf("IE101-1", "MATH116"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "PE014",
             "name" to "Physical Activities Toward Health and Fitness 4",
             "units" to 2,
             "prerequisites" to listOf("PE011", "PE012"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         ),
         mapOf(
             "code" to "SS036",
             "name" to "Science, Technology, and Society",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 2
         )
     )
     val thirdYearTerm1Courses = listOf(
         mapOf(
             "code" to "ACT099",
             "name" to "Accounting for Non-Accountant",
             "units" to 1,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CPE101-2",
             "name" to "Logic Circuits and Switching Theory for EE",
             "units" to 2,
             "prerequisites" to listOf("ECE101-4"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CPE101L-2",
             "name" to "Logic Circuits and Switching Theory for EE (Lab)",
             "units" to 1,
             "prerequisites" to listOf("ECE101L-4"),
             "coRequisites" to listOf("CPE101-2"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CPE126",
             "name" to "Artificial Intelligence",
             "units" to 2,
             "prerequisites" to listOf("DS100L"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CPE126L",
             "name" to "Artificial Intelligence (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("DS100L"),
             "coRequisites" to listOf("CPE126"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "ECE130",
             "name" to "Feedback and Control Systems",
             "units" to 3,
             "prerequisites" to listOf("MATH116"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE107-1",
             "name" to "AC Machinery",
             "units" to 3,
             "prerequisites" to listOf("EE106-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE107L-1",
             "name" to "AC Machinery (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE106L-1"),
             "coRequisites" to listOf("EE107-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE122-1",
             "name" to "AC Apparatus and Devices",
             "units" to 2,
             "prerequisites" to listOf("EE103"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE122L-1",
             "name" to "AC Apparatus and Devices (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE103L"),
             "coRequisites" to listOf("EE122-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE801E",
             "name" to "Engineering Sciences Exit Exam",
             "units" to 0,
             "prerequisites" to listOf("CHM031", "PHY035", "EECO102", "MEC100-1", "ME111-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "HUM034",
             "name" to "Art Appreciation",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 3
         )
     )

     val thirdYearTerm2Courses = listOf(
         mapOf(
             "code" to "CPE103-2",
             "name" to "Microprocessor Systems for Electrical Engineers",
             "units" to 2,
             "prerequisites" to listOf("CPE101-2"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CPE103L-2",
             "name" to "Microprocessor Systems for Electrical Engineers (Lab)",
             "units" to 1,
             "prerequisites" to listOf("CPE101L-2"),
             "coRequisites" to listOf("CPE103-2"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EE109-2",
             "name" to "Electrical Systems and Illumination Engineering Design",
             "units" to 3,
             "prerequisites" to listOf("EE107-1", "EE122L-1"),
             "coRequisites" to listOf("EE182C"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EE109L-2",
             "name" to "Electrical Systems Design (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE107L-1", "EE122L-1"),
             "coRequisites" to listOf("EE109-2"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EE109L-3",
             "name" to "Illumination Engineering Design (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE107L-1", "EE122L-1"),
             "coRequisites" to listOf("EE109-2"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE182C",
             "name" to "Electrical Standards and Practices (Computational)",
             "units" to 1,
             "prerequisites" to listOf("EE181"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "EE802E",
             "name" to "Electrical Engineering 1 Exit Exam",
             "units" to 0,
             "prerequisites" to listOf("CPE101-2", "EE107-1", "EE122-1"),
             "coRequisites" to listOf("EE182C"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "GEELEC01",
             "name" to "GE Elective 1",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "MSE102",
             "name" to "Fundamentals of Material Science and Engineering",
             "units" to 3,
             "prerequisites" to listOf("CHM031", "CE104-2"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "RES101",
             "name" to "Methods of Research",
             "units" to 3,
             "prerequisites" to listOf("IE101-1", "ENG024"),
             "coRequisites" to listOf("CPE103-2"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         ),

         mapOf(
             "code" to "SS022",
             "name" to "Readings in Philippine History",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 3
         )
     )
     val thirdYearTerm3Courses = listOf(
         mapOf(
             "code" to "CAP200D",
             "name" to "Capstone Design/ Thesis 1 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("RES101"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "CE120-1",
             "name" to "Fluid Mechanics for EE",
             "units" to 2,
             "prerequisites" to listOf("MEC100-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "ECE103-1",
             "name" to "Industrial Electronics for Electrical Engineers",
             "units" to 3,
             "prerequisites" to listOf("ECE101-4"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "ECE103L-1",
             "name" to "Industrial Electronics for Electrical Engineers (Lab)",
             "units" to 1,
             "prerequisites" to listOf("ECE101L-4"),
             "coRequisites" to listOf("ECE103-1"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EE134-1",
             "name" to "Power Systems Analysis 1",
             "units" to 2,
             "prerequisites" to listOf("EE182C", "EE122-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EE134L-1",
             "name" to "Power Systems Analysis 1 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE182C", "EE122L-1"),
             "coRequisites" to listOf("EE134-1"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "EEELEC01",
             "name" to "EE Elective 1",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "SS085",
             "name" to "Philippine Indigenous Communities",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1, 2, 3),
             "term" to 3,
             "yearLevel" to 3
         ),
         mapOf(
             "code" to "TEC100-2",
             "name" to "Technopreneurship",
             "units" to 3,
             "prerequisites" to listOf("EMGT100", "ACT099"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2, 3),
             "term" to 3,
             "yearLevel" to 3
         )
     )
     val fourthYearTerm1Courses = listOf(
         mapOf(
             "code" to "CAP200D-1",
             "name" to "Capstone Design/ Thesis 2 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("CAP200D"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "ECE132-1",
             "name" to "Instrumentation and Control",
             "units" to 2,
             "prerequisites" to listOf("ECE130"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "ECE132L-1",
             "name" to "Instrumentation and Control (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("ECE130"),
             "coRequisites" to listOf("ECE132-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE140-1",
             "name" to "Principles of Communication System for EE",
             "units" to 3,
             "prerequisites" to listOf("ECE101-4"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE135-1",
             "name" to "Power Systems Analysis 2",
             "units" to 2,
             "prerequisites" to listOf("EE134-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE135L-1",
             "name" to "Power Systems Analysis 2 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("EE134L-1"),
             "coRequisites" to listOf("EE135-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE136L-1",
             "name" to "Fundamentals of Power Plant Engineering Design (Lab)",
             "units" to 1,
             "prerequisites" to listOf("EE134-1", "EE134L-1"),
             "coRequisites" to listOf("EE135L-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE803E",
             "name" to "Electrical Engineering 2 Exit Exam",
             "units" to 0,
             "prerequisites" to listOf("EE107-1", "EE122-1", "EE182C", "EE109-2", "EE134-1"),
             "coRequisites" to listOf("ECE140-1"),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EEELEC02",
             "name" to "EE Elective 2",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "SS086",
             "name" to "Gender and Society",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE198-3",
             "name" to "EE Correlation 1",
             "units" to -1,
             "prerequisites" to listOf("MATH800E", "EE801E"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(1),
             "term" to 1,
             "yearLevel" to 4
         )
     )
     val fourthYearTerm2Courses = listOf(
         mapOf(
             "code" to "CAP200D-2",
             "name" to "Capstone Design/ Thesis 3 (Laboratory)",
             "units" to 1,
             "prerequisites" to listOf("CAP200D-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE137-1",
             "name" to "Distribution Systems and Substation Design",
             "units" to 2,
             "prerequisites" to listOf("EE134-1"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE137L-1",
             "name" to "Distribution Systems and Substation Design (Lab)",
             "units" to 1,
             "prerequisites" to listOf("EE134-1"),
             "coRequisites" to listOf("EE137-1"),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EEELEC03",
             "name" to "EE Elective 3",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "SS023",
             "name" to "The Contemporary World",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "SS038",
             "name" to "The Life and Works of Jose Rizal",
             "units" to 3,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE198-4",
             "name" to "EE Correlation 2",
             "units" to -1,
             "prerequisites" to listOf("EE134-1", "ECE132-1", "ECE140-1", "EE802E", "EE803E"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(2),
             "term" to 2,
             "yearLevel" to 4
         )
     )
     val fourthYearTerm3Courses = listOf(
         mapOf(
             "code" to "EE191T-1",
             "name" to "EE Seminars and Colloquium (Field)",
             "units" to 1,
             "prerequisites" to listOf("EE162C"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "EE199R-1",
             "name" to "EE Practicum",
             "units" to 3,
             "prerequisites" to listOf("EE102-1", "ECE101-4", "HUM039"),
             "coRequisites" to listOf("SAF102"),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "SAF102",
             "name" to "Basic Occupational Safety and Health",
             "units" to 3,
             "prerequisites" to listOf("CHM031"),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 4
         ),
         mapOf(
             "code" to "SGE101",
             "name" to "Student Global Experience",
             "units" to 0,
             "prerequisites" to listOf<String>(),
             "coRequisites" to listOf<String>(),
             "regularTerms" to listOf(3),
             "term" to 3,
             "yearLevel" to 4
         )
     )

     val electives = mapOf(
         "OPEN_ELECTIVE" to listOf(
             mapOf("code" to "EE194", "name" to "Special Topics in Electrical Engineering", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         ),
         "GEN_ED" to listOf(
             mapOf("code" to "ENV075", "name" to "Environmental Science", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "ENV076", "name" to "People and the Earth's Ecosystem", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "ENT078", "name" to "The Entrepreneurial Mind", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "HUM080", "name" to "Philippine Popular Culture", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "HUM081", "name" to "Indigenous Creative Crafts", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "HUM082", "name" to "Reading Visual Art", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "HUM083", "name" to "Great Books", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4),
             mapOf("code" to "SS084", "name" to "Religions, Religious Experiences and Spirituality", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 4)
         ),
         "ADVANCED_POWER_SYSTEMS" to listOf(
             mapOf("code" to "EE136-1", "name" to "Advanced Power Systems Analysis: Economic Operation and Control", "units" to 3, "prerequisites" to listOf("EE182C"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE136-2", "name" to "Smart Grid Applications in Power Systems", "units" to 3, "prerequisites" to listOf("EE182C"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE136-3", "name" to "Maintenance of Power Generators and Substation Equipment", "units" to 3, "prerequisites" to listOf("EE182C"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE135-2", "name" to "Power System Protection", "units" to 3, "prerequisites" to listOf("EE134-1", "EE134L-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         ),
         "ADVANCED_ELECTRICAL_SYSTEMS_DESIGN" to listOf(
             mapOf("code" to "EE109P-1", "name" to "Advanced Electrical System Design: High-Rise and Industrial Buildings (Paired)", "units" to 3, "prerequisites" to listOf("EE109-2", "EE109L-3"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE132P-1", "name" to "Advanced Power System Design: Substation and Distribution (Paired)", "units" to 3, "prerequisites" to listOf("EE109-2", "EE109L-3"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         ),
         "AGRICULTURAL_ENGINEERING" to listOf(
             mapOf("code" to "AENG012", "name" to "AB Power Engineering", "units" to 3, "prerequisites" to listOf("EE102-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "AENG013", "name" to "AB Electrification and Control Systems", "units" to 3, "prerequisites" to listOf("AENG012"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         ),
         "MECHATRONICS" to listOf(
             mapOf("code" to "IAS101P-1", "name" to "Mechatronics 1: Fundamentals of Mechatronics (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "IAS101P-2", "name" to "Mechatronics 2: Industrial Control Mechanisms (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "IAS101P-3", "name" to "Mechatronics 3: Advanced Supervisory Control and Data Acquisition System (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         ),
         "ADVANCED_SYSTEM_DESIGN" to listOf(
             mapOf("code" to "EE111P-1", "name" to "Advanced Electrical and Illumination System Design: Residential and Commercial Buildings (Paired)", "units" to 3, "prerequisites" to listOf("EE109-2", "EE109L-3"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE111P-2", "name" to "Advanced Electrical Systems Design: High-Rise and Industrial Buildings (Paired)", "units" to 3, "prerequisites" to listOf("EE111P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3),
             mapOf("code" to "EE111P-3", "name" to "Advanced Power System Design: Substation and Distribution (Paired)", "units" to 3, "prerequisites" to listOf("EE111P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1, 2, 3), "term" to 3, "yearLevel" to 3)
         )
     )


     repository.uploadCourses("bsee_2024_2025", 1, "term_1", firstYearTerm1Courses) {
         Log.d("UploadDebug", "First Year Term 1 courses uploaded")
     }

     repository.uploadCourses("bsee_2024_2025", 1, "term_2", firstYearTerm2Courses) {
         Log.d("UploadDebug", "First Year Term 2 courses uploaded")
     }
     repository.uploadCourses("bsee_2024_2025", 1, "term_3", firstYearTerm3Courses) {
         Log.d("UploadDebug", "First Year Term 3 courses uploaded")
     }

     repository.uploadCourses("bsee_2024_2025", 2, "term_1", secondYearTerm1Courses) {
         Log.d("UploadDebug", "Second Year Term 1 courses uploaded")
     }
     repository.uploadCourses("bsee_2024_2025", 2, "term_2", secondYearTerm2Courses) {
         Log.d("UploadDebug", "Second Year Term 2 courses uploaded")
     }

     repository.uploadCourses("bsee_2024_2025", 2, "term_3", secondYearTerm3Courses) {
         Log.d("UploadDebug", "Second Year Term 3 courses uploaded")
     }
     repository.uploadCourses("bsee_2024_2025", 3, "term_1", thirdYearTerm1Courses) {
         Log.d("UploadDebug", "Third Year Term 1 courses uploaded successfully!")
     }

     repository.uploadCourses("bsee_2024_2025", 3, "term_2", thirdYearTerm2Courses) {
         Log.d("UploadDebug", "Third Year Term 2 courses uploaded successfully!")
     }
     repository.uploadCourses("bsee_2024_2025", 3, "term_3", thirdYearTerm3Courses) {
         Log.d("UploadDebug", "Third Year Term 3 courses uploaded successfully!")
     }

     repository.uploadCourses("bsee_2024_2025", 4, "term_1", fourthYearTerm1Courses) {
         Log.d("UploadDebug", "Fourth Year Term 1 courses uploaded successfully!")
     }

     repository.uploadCourses("bsee_2024_2025", 4, "term_2", fourthYearTerm2Courses) {
         Log.d("UploadDebug", "Fourth Year Term 2 courses uploaded successfully!")
     }

     repository.uploadCourses("bsee_2024_2025", 4, "term_3", fourthYearTerm3Courses) {
         Log.d("UploadDebug", "Fourth Year Term 3 courses uploaded successfully!")
     }

     repository.uploadElectives("bsee_2024_2025", electives)

 }





    // duff curriculum
    fun uploadBSCPE_2021_2022() {
        val firstYearTerm1Courses = listOf(
            mapOf(
                "code" to "CHM031",
                "name" to "Chemistry for Engineers",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "CHM031L",
                "name" to "Chemistry for Engineers (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf("CHM031"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "ENG023",
                "name" to "Receptive Communication Skills",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "HUM021",
                "name" to "Logic and Critical Thinking",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "MATH031",
                "name" to "Mathematics for Engineers",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "SS022",
                "name" to "Readings in Philippine History",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "VE021",
                "name" to "Life Coaching Series 1",
                "units" to -1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 1
            )
        )

        val firstYearTerm2Courses = listOf(
            mapOf(
                "code" to "CPE001L",
                "name" to "Computer Fundamentals and Programming 1 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "CPE100",
                "name" to "Computer Engineering As A Discipline",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "DRAW021W",
                "name" to "Engineering Drawing 1",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "ENG024",
                "name" to "Writing for Academic Studies",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "MATH035",
                "name" to "Mathematics in the Modern World",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "MATH041",
                "name" to "Engineering Calculus 1",
                "units" to 4,
                "prerequisites" to listOf("MATH031"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "SS021",
                "name" to "Understanding the Self",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "VE022",
                "name" to "Life Coaching Series 2",
                "units" to -1,
                "prerequisites" to listOf("VE021"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 1
            )

        )

        val firstYearTerm3Courses = listOf(
            mapOf(
                "code" to "DRAW023L-1",
                "name" to "Computer-Aided Drafting (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("DRAW021W"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "CPE141L",
                "name" to "Programming Logic and Design (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE001L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "HUM039",
                "name" to "Ethics",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "MATH042",
                "name" to "Engineering Calculus 2",
                "units" to 4,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "PHY035",
                "name" to "Physics for Engineers",
                "units" to 4,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "PHY035L",
                "name" to "Physics for Engineers (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf("PHY035"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            ),
            mapOf(
                "code" to "VE023",
                "name" to "Life Coaching Series 3",
                "units" to -1,
                "prerequisites" to listOf("VE022"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 1
            )
        )

        val secondYearTerm1Courses = listOf(
            mapOf(
                "code" to "CPE105-1",
                "name" to "Discrete Mathematics",
                "units" to 3,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "CPE142L",
                "name" to "Object Oriented Programing (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE001L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "ECE121L",
                "name" to "Computer-Aided Calculations (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "EE101-3",
                "name" to "Fundamentals of Electrical Circuits",
                "units" to 3,
                "prerequisites" to listOf("PHY035L", "MATH042"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "EE101L-3",
                "name" to "Fundamentals of Electrical Circuits (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("PHY035L"),
                "coRequisites" to listOf("EE101-3"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "IE101-1",
                "name" to "Engineering Data Analysis",
                "units" to 3,
                "prerequisites" to listOf("MATH041"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH056",
                "name" to "Differential Equations",
                "units" to 3,
                "prerequisites" to listOf("MATH042"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "NSTP010",
                "name" to "National Service Training Program 1",
                "units" to -3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 2
            )
        )
        val secondYearTerm2Courses = listOf(
            mapOf(
                "code" to "ECE101-3",
                "name" to "Fundamental of Electronic Circuits",
                "units" to 3,
                "prerequisites" to listOf("EE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "ECE101L-3",
                "name" to "Fundamental of Electronic Circuits (Lab)",
                "units" to 1,
                "prerequisites" to listOf("EE101L-3"),
                "coRequisites" to listOf("ECE101-3"),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "EECO102",
                "name" to "Engineering Economy",
                "units" to 3,
                "prerequisites" to listOf("IE101-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "ENG041",
                "name" to "Purposive Communication",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "GEELEC01",
                "name" to "GE ELECTIVE 1",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH116",
                "name" to "Advanced Engineering Mathematics",
                "units" to 3,
                "prerequisites" to listOf("MATH056"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH116E",
                "name" to "Engineering Mathematics Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("IE101-1", "MATH056"),
                "coRequisites" to listOf("MATH116"),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "SS023",
                "name" to "The Contemporary World",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "NSTP011P",
                "name" to "National Service Training Program 2 (Paired)",
                "units" to -3,
                "prerequisites" to listOf("NSTP010"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 2
            )
        )

        val secondYearTerm3Courses = listOf(
            mapOf(
                "code" to "CPE101-1",
                "name" to "Digital Electronics: Logic Circuits and Design",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "CPE101L-1",
                "name" to "Digital Electronics: Logic Circuits and Design (LAB)",
                "units" to 1,
                "prerequisites" to listOf("ECE101L-3"),
                "coRequisites" to listOf("CPE101-1"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "CPE106-1",
                "name" to "Data and Digital Communications",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "CPE121L-1",
                "name" to "Computer Engineering Drafting and Design (Lab)",
                "units" to 1,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "CS105L",
                "name" to "Data Structures and Algorithms (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE141L", "CPE142L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH161",
                "name" to "Numerical Methods",
                "units" to 3,
                "prerequisites" to listOf("MATH116"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH161L",
                "name" to "Numerical Methods (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("MATH116", "ECE121L"),
                "coRequisites" to listOf<String>("MATH116"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH161",
                "name" to "Numerical Methods",
                "units" to 3,
                "prerequisites" to listOf("MATH116"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "MATH161L",
                "name" to "Numerical Methods (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("MATH116", "ECE121L"),
                "coRequisites" to listOf("MATH161"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "EENV102",
                "name" to "Environmental Science and Engineering",
                "units" to 3,
                "prerequisites" to listOf("CHM031"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "EMGT100",
                "name" to "Engineering Management",
                "units" to 2,
                "prerequisites" to listOf("EECO102"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            ),
            mapOf(
                "code" to "PE001",
                "name" to "Physical Activity Towards Health and Fitness 1",
                "units" to -2,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 2
            )
        )
        val thirdYearTerm1Courses = listOf(
            mapOf(
                "code" to "CPE103-4",
                "name" to "Microprocessors",
                "units" to 3,
                "prerequisites" to listOf("CPE101-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE103L-4",
                "name" to "Microprocessors (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE101L-1"),
                "coRequisites" to listOf("CPE103-4"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE109-1",
                "name" to "Computer Nerworks and Security",
                "units" to 3,
                "prerequisites" to listOf("CPE106-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE109L-1",
                "name" to "Computer Nerworks and Security (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE106-1"),
                "coRequisites" to listOf("CPE109-1"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE143L",
                "name" to "Web Design and Development (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE142L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "ECE130",
                "name" to "Feedback and Control Systems",
                "units" to 3,
                "prerequisites" to listOf("MATH116"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "GEELEC02",
                "name" to "GE ELECTIVE 2",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "TEC100",
                "name" to "Technopreneurship",
                "units" to 3,
                "prerequisites" to listOf("EMGT100"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "PE002",
                "name" to "Physical Activity Towards Health and Fitness 2",
                "units" to -2,
                "prerequisites" to listOf("PE001"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 3
            )
        )

        val thirdYearTerm2Courses = listOf(
            mapOf(
                "code" to "CPE104l-1",
                "name" to "Introduction to Hardware Description Language",
                "units" to 1,
                "prerequisites" to listOf("CPE141L", "ECE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE107-1",
                "name" to "Software Design",
                "units" to 3,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE107L-1",
                "name" to "Software Design (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf("CPE107-1"),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE108P",
                "name" to "Fundamentals of Mixed Signals and Sensors",
                "units" to 3,
                "prerequisites" to listOf("ECE101-3"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE108E",
                "name" to "General Engineering and Applied Sciences Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CHE031", "PHY035","EECO102"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "CPE144L",
                "name" to "Mobile Application Development (Laboratory)",
                "units" to 2,
                "prerequisites" to listOf("CPE142L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "NETA172P-1",
                "name" to "CCNA Routing and Switching 1 (Paired)",
                "units" to 3,
                "prerequisites" to listOf("CPE109-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "RES101",
                "name" to "Methods of Research",
                "units" to 3,
                "prerequisites" to listOf("IE101-1","ENG024"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "SS085",
                "name" to "Philippine Indigenous Communities",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            ),

            mapOf(
                "code" to "PE003",
                "name" to "Physical Activity Towards Health and Fitness 3",
                "units" to -2,
                "prerequisites" to listOf("PE002"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 3
            )
        )
        val thirdYearTerm3Courses = listOf(
            mapOf(
                "code" to "CAP200D",
                "name" to "Capstone Design/ Thesis 1 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("RES101"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE112-1",
                "name" to "Embedded Systems",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE112L-1",
                "name" to "Embedded Systems (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CPE103L-4"),
                "coRequisites" to listOf("CPE112-1"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE144E",
                "name" to "Computer Engineering 1 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE142L", "CPE143L", "CS105L","CPE107-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE151",
                "name" to "Operating Systems",
                "units" to 3,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPE151L",
                "name" to "Operating Systems (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CS105L"),
                "coRequisites" to listOf("CPE151"),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "CPEELEC01",
                "name" to "CPE Elective 1 (Paired)",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "HUM034",
                "name" to "Art Appreciation",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1, 2, 3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "SS036",
                "name" to "Science, Technology, and Society",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1,2, 3),
                "term" to 3,
                "yearLevel" to 3
            ),
            mapOf(
                "code" to "PE004",
                "name" to "Physical Activity Towards Health and Fitness 4",
                "units" to -2,
                "prerequisites" to listOf("PE003"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1, 2, 3),
                "term" to 3,
                "yearLevel" to 3
            )

        )
        val fourthYearTerm1Courses = listOf(
            mapOf(
                "code" to "CAP200D-1",
                "name" to "Capstone Design/ Thesis 2 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CAP200D"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE110-1",
                "name" to "Emerging Technologies in CPE",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE112-1E",
                "name" to "Computer Engineering 2 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE103-4", "CPE104L-1", "CPE108P", "EE101-3"),
                "coRequisites" to listOf("CPE112-1"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE113-1",
                "name" to "Digital Signal Processing",
                "units" to 3,
                "prerequisites" to listOf("ECE130"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE113L-1",
                "name" to "Digital Signal Processing (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("ECE130", "ECE121L"),
                "coRequisites" to listOf("CPE113-1"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE131-1",
                "name" to "Computer Architecture and Organization",
                "units" to 3,
                "prerequisites" to listOf("CPE103-4"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE131L-1",
                "name" to "Computer Architecture and Organization (Lab)",
                "units" to 1,
                "prerequisites" to listOf("CPE103L-4"),
                "coRequisites" to listOf("CPE131-1"),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPEELEC02",
                "name" to "CPE Elective 2 (Paired)",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "COR198",
                "name" to "Correlation 1",
                "units" to -1,
                "prerequisites" to listOf("CHM031", "MATH116", "MATH116E", "PHY035", "EECO102"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(1),
                "term" to 1,
                "yearLevel" to 4
            )
        )
        val fourthYearTerm2Courses = listOf(
            mapOf(
                "code" to "CAP200D-2",
                "name" to "Capstone Design/ Thesis 3 (Laboratory)",
                "units" to 1,
                "prerequisites" to listOf("CAP200D-1"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE181",
                "name" to "CPE Laws and Professional Practice",
                "units" to 2,
                "prerequisites" to listOf("HUM039"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE181E",
                "name" to "Computer Engineering 3 Exit Exam",
                "units" to 0,
                "prerequisites" to listOf("CPE109-1", "CPE110-1", "CPE113-1", "CPE151"),
                "coRequisites" to listOf("CPE131-1", "CPE181"),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPEELEC03",
                "name" to "CPE Elective 3 (Paired)",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "SS038",
                "name" to "The Life and Works of Jose Rizal",
                "units" to 3,
                "prerequisites" to listOf<String>(),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE198-2",
                "name" to "CPE Correlation 2",
                "units" to -1,
                "prerequisites" to listOf("CPE112-1", "CPE113-1", "CPE151","CPE108E","CPE144E","CPE112-1E"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(2),
                "term" to 2,
                "yearLevel" to 4
            )
        )
        val fourthYearTerm3Courses = listOf(
            mapOf(
                "code" to "CPE199R-1",
                "name" to "CPE Practicum",
                "units" to 3,
                "prerequisites" to listOf("CPE143L", "CS105L", "ECE101-3","HUM039"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "CPE191F-1",
                "name" to "CPE Seminars and Field Trips (Field)",
                "units" to 1,
                "prerequisites" to listOf("CPE10-1", "CPE144L"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 4
            ),
            mapOf(
                "code" to "SAF102",
                "name" to "Basic Occupational Safety and Health",
                "units" to 3,
                "prerequisites" to listOf("CHM031"),
                "coRequisites" to listOf<String>(),
                "regularTerms" to listOf(3),
                "term" to 3,
                "yearLevel" to 4
            )
        )

        val electives = mapOf(
            "NETA172P" to listOf(
                mapOf("code" to "NETA172P-2", "name" to "CCNA Routing and Switching 2 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "NETA172P-3", "name" to "CCNA Routing and Switching 3 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "NETA172P-4", "name" to "CCNA Routing and Switching 4 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "MICR172P" to listOf(
                mapOf("code" to "MICR172P-1", "name" to "Microelectronics 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE103-4", "CPE104L-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "MICR172P-2", "name" to "Microelectronics 2 (Paired)", "units" to 3, "prerequisites" to listOf("MICR172P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "MICR172P-3", "name" to "Microelectronics 3 (Paired)", "units" to 3, "prerequisites" to listOf("MICR172P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "GEN_ED" to listOf(
                mapOf("code" to "ENV075", "name" to "Environmental Science", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ENV076", "name" to "People and the Earth's Ecosystem", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ENV078", "name" to "The Entrepreneurial Mind", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM080", "name" to "Philippine Popular Culture", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM081", "name" to "Indigenous Creative Crafts", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM082", "name" to "Reading Visual Art", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM083", "name" to "Great Books", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SS084", "name" to "Religions, Religious Experiences and Spirituality", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SS086", "name" to "Gender and Society", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "MACH171P" to listOf(
                mapOf("code" to "MACH171P-1", "name" to "Machine Learning 1 (Paired)", "units" to 3, "prerequisites" to listOf("MATH116", "CS105L"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "MACH171P-2", "name" to "Machine Learning 2 (Paired)", "units" to 3, "prerequisites" to listOf("MACH171P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "MACH171P-3", "name" to "Machine Learning 3 (Paired)", "units" to 3, "prerequisites" to listOf("MACH171P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "EMSY171P" to listOf(
                mapOf("code" to "EMSY171P-1", "name" to "Embedded Systems 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE103-4", "CPE104L-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "EMSY171P-2", "name" to "Embedded Systems 2 (Paired)", "units" to 3, "prerequisites" to listOf("EMSY171P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "EMSY171P-3", "name" to "Embedded Systems 3 (Paired)", "units" to 3, "prerequisites" to listOf("EMSY171P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "SDEV173P" to listOf(
                mapOf("code" to "SDEV173P-1", "name" to "Software Development 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE107-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "SDEV173P-2", "name" to "Software Development 2 (Paired)", "units" to 3, "prerequisites" to listOf("SDEV173P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "SDEV173P-3", "name" to "Software Development 3 (Paired)", "units" to 3, "prerequisites" to listOf("SDEV173P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "SNAD174P" to listOf(
                mapOf("code" to "SNAD174P-1", "name" to "System and Network Administration 1 (Paired)", "units" to 3, "prerequisites" to listOf("CPE109-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "SNAD174P-2", "name" to "System and Network Administration 2 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD174P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "SNAD174P-3", "name" to "System and Network Administration 3 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD174P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            ),
            "AWS171P" to listOf(
                mapOf("code" to "AWS171P", "name" to "Cloud Foundations (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "AWS171P-1", "name" to "Cloud Development (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "AWS171P-2", "name" to "Cloud Architecture (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3),
                mapOf("code" to "AWS171P-3", "name" to "Cloud System Operations (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 3)
            )
        )


        repository.uploadCourses("bscpe_2021_2022", 1, "term_1", firstYearTerm1Courses) {
            Log.d("UploadDebug", "First Year Term 1 courses uploaded")
        }

        repository.uploadCourses("bscpe_2021_2022", 1, "term_2", firstYearTerm2Courses) {
            Log.d("UploadDebug", "First Year Term 2 courses uploaded")
        }
        repository.uploadCourses("bscpe_2021_2022", 1, "term_3", firstYearTerm3Courses) {
            Log.d("UploadDebug", "First Year Term 3 courses uploaded")
        }

        repository.uploadCourses("bscpe_2021_2022", 2, "term_1", secondYearTerm1Courses) {
            Log.d("UploadDebug", "Second Year Term 1 courses uploaded")
        }
        repository.uploadCourses("bscpe_2021_2022", 2, "term_2", secondYearTerm2Courses) {
            Log.d("UploadDebug", "Second Year Term 2 courses uploaded")
        }

        repository.uploadCourses("bscpe_2021_2022", 2, "term_3", secondYearTerm3Courses) {
            Log.d("UploadDebug", "Second Year Term 3 courses uploaded")
        }
        repository.uploadCourses("bscpe_2021_2022", 3, "term_1", thirdYearTerm1Courses) {
            Log.d("UploadDebug", "Third Year Term 1 courses uploaded successfully!")
        }

        repository.uploadCourses("bscpe_2021_2022", 3, "term_2", thirdYearTerm2Courses) {
            Log.d("UploadDebug", "Third Year Term 2 courses uploaded successfully!")
        }
        repository.uploadCourses("bscpe_2021_2022", 3, "term_3", thirdYearTerm3Courses) {
            Log.d("UploadDebug", "Third Year Term 3 courses uploaded successfully!")
        }

        repository.uploadCourses("bscpe_2021_2022", 4, "term_1", fourthYearTerm1Courses) {
            Log.d("UploadDebug", "Fourth Year Term 1 courses uploaded successfully!")
        }

        repository.uploadCourses("bscpe_2021_2022", 4, "term_2", fourthYearTerm2Courses) {
            Log.d("UploadDebug", "Fourth Year Term 2 courses uploaded successfully!")
        }

        repository.uploadCourses("bscpe_2021_2022", 4, "term_3", fourthYearTerm3Courses) {
            Log.d("UploadDebug", "Fourth Year Term 3 courses uploaded successfully!")
        }

        repository.uploadElectives("bscpe_2021_2022", electives)

    }

    //bs ece 2022-2023 curriculum
    fun uploadbsECE2022Curriculum() {
        val firstYearTerm1Courses = listOf(
            mapOf("code" to "CHM031", "name" to "Chemistry for Engineers", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "CHM031L", "name" to "Chemistry for Engineers (Laboratory)", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf("CHM031"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "ENG023", "name" to "Receptive Communication Skills", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "HUM021", "name" to "Logic and Critical Thinking", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "MATH031", "name" to "Mathematics for Engineers", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "SS022", "name" to "Readings in Philippine History", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "NSTP010", "name" to "National Service Training Program 1", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1),
            mapOf("code" to "VE021", "name" to "Life Coaching Series 1", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 1)
        )

        val firstYearTerm2Courses = listOf(
            mapOf("code" to "CPE001L", "name" to "Computer Fundamentals and Programming 1 (Lab)", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "DRAW021W", "name" to "Engineering Drawing 1", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "ECE100", "name" to "Electronics Engineering Orientation", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "ENG024", "name" to "Writing for Academic Studies", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "MATH035", "name" to "Mathematics in the Modern World", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "MATH041", "name" to "Engineering Calculus 1", "units" to 4, "prerequisites" to listOf("MATH031"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "SS021", "name" to "Understanding the Self", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "NSTP011P", "name" to "National Service Training Program 2 (Paired)", "units" to 3, "prerequisites" to listOf("NSTP010"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1),
            mapOf("code" to "VE022", "name" to "Life Coaching Series 2", "units" to 1, "prerequisites" to listOf("VE021"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 1)
        )

        val firstYearTerm3Courses = listOf(
            mapOf("code" to "ECE121L", "name" to "Computer-Aided Calculations (Laboratory)", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "HUM039", "name" to "Ethics", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "MATH042", "name" to "Engineering Calculus 2", "units" to 4, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "IE101-1", "name" to "Engineering Data Analysis", "units" to 3, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "PHY035", "name" to "Physics for Engineers", "units" to 4, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "PHY035L", "name" to "Physics for Engineers (Laboratory)", "units" to 1, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf("PHY035"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "PE001", "name" to "Physical Activities Toward Health and Fitness 1", "units" to 2, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1),
            mapOf("code" to "VE023", "name" to "Life Coaching Series 3", "units" to 1, "prerequisites" to listOf("VE022"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 1)
        )

        val secondYearTerm1Courses = listOf(
            mapOf("code" to "CPE142L", "name" to "Object Oriented Programming (Lab)", "units" to 2, "prerequisites" to listOf("CPE001L"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "EECO102", "name" to "Engineering Economy", "units" to 3, "prerequisites" to listOf("IE101-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "EE101-2", "name" to "Circuits 1", "units" to 3, "prerequisites" to listOf("MATH042", "PHY035"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "EE101L-2", "name" to "Circuits 1 (Laboratory)", "units" to 1, "prerequisites" to listOf("PHY035L"), "coRequisites" to listOf("EE101-2"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "MATH056", "name" to "Differential Equations", "units" to 3, "prerequisites" to listOf("MATH042"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "PHY034", "name" to "Physics for ECE", "units" to 3, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "PHY034L", "name" to "Physics for ECE (Laboratory)", "units" to 1, "prerequisites" to listOf("MATH041"), "coRequisites" to listOf("PHY034"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2),
            mapOf("code" to "PE002", "name" to "Physical Activities Toward Health and Fitness 2", "units" to 2, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 2)
        )

        val secondYearTerm2Courses = listOf(
            mapOf("code" to "ECE101-2", "name" to "Electronics Devices and Circuits", "units" to 3, "prerequisites" to listOf("MATH042", "PHY035"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "ECE101L-2", "name" to "Electronics Devices and Circuits (Laboratory)", "units" to 1, "prerequisites" to listOf("PHY035L"), "coRequisites" to listOf("ECE101-2"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "EE102-1", "name" to "Circuits 2", "units" to 3, "prerequisites" to listOf("EE101-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "EE102L-1", "name" to "Circuits 2 (Laboratory)", "units" to 1, "prerequisites" to listOf("EE101L-2"), "coRequisites" to listOf("EE102-1"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "EMGT100", "name" to "Engineering Management", "units" to 2, "prerequisites" to listOf("EECO102"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "MATH116", "name" to "Advanced Engineering Mathematics", "units" to 3, "prerequisites" to listOf("MATH056"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "MATH116E", "name" to "Engineering Mathematics Exit Exam", "units" to 0, "prerequisites" to listOf("IE101-1", "MATH056"), "coRequisites" to listOf("MATH116"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "SS085", "name" to "Philippine Indigenous Communities", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2),
            mapOf("code" to "PE003", "name" to "Physical Activities Toward Health and Fitness 3", "units" to 2, "prerequisites" to listOf("PE001", "PE002"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 2)
        )

        val secondYearTerm3Courses = listOf(
            mapOf("code" to "CPE115-1", "name" to "Logic Circuit and Switching Theory", "units" to 3, "prerequisites" to listOf("ECE101-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "CPE115L-1", "name" to "Logic Circuit and Switching Theory (Laboratory)", "units" to 1, "prerequisites" to listOf("ECE101L-2"), "coRequisites" to listOf("CPE115-1"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "ECE102-1", "name" to "Electronics Circuits Analysis and Design", "units" to 3, "prerequisites" to listOf("ECE101-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "ECE102L-1", "name" to "Electronics Circuits Analysis and Design (Lab)", "units" to 1, "prerequisites" to listOf("ECE101L-2"), "coRequisites" to listOf("ECE102-1"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "ECE115", "name" to "Electromagnetics for ECE", "units" to 4, "prerequisites" to listOf("PHY035", "MATH056"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "MATH116L", "name" to "Advanced Engineering Mathematics (Lab)", "units" to 1, "prerequisites" to listOf("MATH116", "ECE121L"), "coRequisites" to listOf("MATH161"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "MATH161", "name" to "Numerical Methods", "units" to 3, "prerequisites" to listOf("MATH116"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "PE004", "name" to "Physical Activities Toward Health and Fitness 4", "units" to 2, "prerequisites" to listOf("PE001", "PE002"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            // Summer Term Courses
            mapOf("code" to "ENG041", "name" to "Purposive Communication", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "EENV102", "name" to "Environmental Science and Engineering", "units" to 3, "prerequisites" to listOf("CHM031"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2),
            mapOf("code" to "SS036", "name" to "Science, Technology, and Society", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 2)
        )
        val thirdYearTerm1Courses = listOf(
            mapOf("code" to "CPE123-1", "name" to "Microprocessor and Microcontroller Systems and Design", "units" to 3, "prerequisites" to listOf("CPE115-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "CPE123L-1", "name" to "Microprocessor and Microcontroller Systems and Design (Lab)", "units" to 1, "prerequisites" to listOf("CPE115L-1"), "coRequisites" to listOf("CPE123-1"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "DRAW023L-1", "name" to "Computer-Aided Drafting (Laboratory)", "units" to 1, "prerequisites" to listOf("DRAW021W"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "ECE130", "name" to "Feedback and Control Systems", "units" to 3, "prerequisites" to listOf("MATH116"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "ECE130L", "name" to "Feedback and Control Systems (Laboratory)", "units" to 1, "prerequisites" to listOf("MATH116", "ECE121L"), "coRequisites" to listOf("ECE130"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "ECE141-1", "name" to "Principles of Communication System", "units" to 3, "prerequisites" to listOf("ECE102-1", "ECE115"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "ECE141L-1", "name" to "Principles of Communication System (Lab)", "units" to 1, "prerequisites" to listOf("ECE102L-1"), "coRequisites" to listOf("ECE141-1"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "EMGT100E", "name" to "General Engineering and Applied Sciences Exit Exam", "units" to 0, "prerequisites" to listOf("CHM031", "PHY035", "PHY034", "EECO102"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3),
            mapOf("code" to "TEC100", "name" to "Technopreneurship", "units" to 3, "prerequisites" to listOf("EMGT100"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 3)
        )

        val thirdYearTerm2Courses = listOf(
            mapOf("code" to "ECE142-1", "name" to "Modulation and Coding Techniques", "units" to 3, "prerequisites" to listOf("ECE141-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "ECE142L-1", "name" to "Modulation and Coding Techniques Laboratory", "units" to 1, "prerequisites" to listOf("ECE141L-1"), "coRequisites" to listOf("ECE142-1"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "ECE163", "name" to "Signals Spectra and Signal Processing", "units" to 3, "prerequisites" to listOf("MATH116"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "ECE163L", "name" to "Signals Spectra and Signal Processing (Lab)", "units" to 1, "prerequisites" to listOf("ECE121L", "MATH116"), "coRequisites" to listOf("ECE163"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "NETA172P-1", "name" to "CCNA Routing and Switching 1 (Paired)", "units" to 3, "prerequisites" to listOf("PHY035"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "RES101", "name" to "Methods of Research", "units" to 3, "prerequisites" to listOf("IE101-1", "ENG024"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3),
            mapOf("code" to "CPE144L", "name" to "Mobile Application Development (Laboratory)", "units" to 2, "prerequisites" to listOf("CPE142L"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 3)
        )

        val thirdYearTerm3Courses = listOf(
            mapOf("code" to "CAP200D", "name" to "Capstone Design / Thesis 1 (Laboratory)", "units" to 1, "prerequisites" to listOf("RES101"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE107-1", "name" to "Electronic Systems and Design", "units" to 3, "prerequisites" to listOf("ECE102-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE107L-1", "name" to "Electronic Systems and Design (Laboratory)", "units" to 1, "prerequisites" to listOf("ECE102-1"), "coRequisites" to listOf("ECE107-1"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE107-1E", "name" to "Electronics Engineering Exit Exam", "units" to 0, "prerequisites" to listOf("ECE102-1", "EE102-1", "CPE115-1", "ECE130", "ECE115", "CPE123-1"), "coRequisites" to listOf("ECE107-1"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE143-1", "name" to "Transmission Media and Antenna Systems and Design", "units" to 3, "prerequisites" to listOf("ECE142-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE143L-1", "name" to "Transmission Media and Antenna Systems and Design (Lab)", "units" to 1, "prerequisites" to listOf("ECE142L-1"), "coRequisites" to listOf("ECE143-1"), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECE160L", "name" to "Electronic Schematics, Diagram and Modules (Lab)", "units" to 1, "prerequisites" to listOf("ECE102-1", "ECE102L-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "ECEELEC01-1", "name" to "ECE Elective 1 (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "GEELEC01", "name" to "GE Elective 1", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3) ,
            mapOf("code" to "GEELEC02", "name" to "GE Elective 2", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "MSE102", "name" to "Fundamentals of Material Science and Engineering", "units" to 3, "prerequisites" to listOf("CHM031"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3),
            mapOf("code" to "SAF102", "name" to "Basic Occupational Safety and Health", "units" to 3, "prerequisites" to listOf("CHM031"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 3)
        )

        val fourthYearTerm1Courses = listOf(
            mapOf("code" to "CAP200D-1", "name" to "Capstone Design / Thesis 2 (Laboratory)", "units" to 1, "prerequisites" to listOf("CAP200D"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "ECE144-1", "name" to "Data Communications", "units" to 3, "prerequisites" to listOf("ECE142-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "ECE144L-1", "name" to "Data Communications (Laboratory)", "units" to 1, "prerequisites" to listOf("ECE142L-1"), "coRequisites" to listOf("ECE144-1"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "ECE144-1E", "name" to "Electronics Systems and Technologies 1 Exit Exam", "units" to 0, "prerequisites" to listOf("ECE142-1", "ECE163", "ECE107-1"), "coRequisites" to listOf("ECE144-1"), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "ECEELEC02-1", "name" to "ECE Elective 2 (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "HUM034", "name" to "Art Appreciation", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "SS023", "name" to "The Contemporary World", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4),
            mapOf("code" to "DS100L", "name" to "Applied Data Science Laboratory", "units" to 1, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1), "term" to 1, "yearLevel" to 4)
        )

        val fourthYearTerm2Courses = listOf(
            mapOf("code" to "CAP200D-2", "name" to "Capstone Design / Thesis 3 (Laboratory)", "units" to 1, "prerequisites" to listOf("CAP200D"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "ECE145P-1", "name" to "Wireless Communications (Paired)", "units" to 4, "prerequisites" to listOf("ECE143-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "ECE181", "name" to "ECE Laws, Codes and Professional Ethics", "units" to 3, "prerequisites" to listOf("HUM039"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "ECE181E", "name" to "Electronics Systems and Technologies 2 Exit Exam", "units" to 0, "prerequisites" to listOf("ECE143-1", "ECE144-1"), "coRequisites" to listOf("ECE145P-1", "ECE181"), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "ECE191F-1", "name" to "ECE Seminars and Colloquium (Field)", "units" to 1, "prerequisites" to listOf("CPE123-1", "ECE141-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "ECEELEC03-1", "name" to "ECE Elective 3 (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "SS038", "name" to "The Life and Works of Jose Rizal", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "COR198", "name" to "Correlation 1", "units" to -1, "prerequisites" to listOf("CHM031", "MATH116", "MATH116E", "PHY035", "EECO102"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4),
            mapOf("code" to "SGE101", "name" to "Student Global Experience", "units" to 0, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(2), "term" to 2, "yearLevel" to 4)
        )

        val fourthYearTerm3Courses = listOf(
            mapOf("code" to "ECE199R-1", "name" to "ECE Practicum", "units" to 3, "prerequisites" to listOf("ECE102-1", "ECE141-1", "HUM039", "CAP200D-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 4),
            mapOf("code" to "ECE198-2", "name" to "ECE Correlation 2", "units" to -1, "prerequisites" to listOf("EMGT100E", "ECE107-1E", "ECE143-1", "ECE144-1", "ECE144-1E"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(3), "term" to 3, "yearLevel" to 4)
        )

        val electives = mapOf(
            "ECE137P" to listOf(
                mapOf("code" to "ECE137P", "name" to "Industrial Control and Instrumentation Systems (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE137P-1", "name" to "Mechatronics 1: Fundamentals of Mechatronics (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE137P-2", "name" to "Mechatronics 2: Robotics (Paired)", "units" to 3, "prerequisites" to listOf("ECE130"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE110P" to listOf(
                mapOf("code" to "ECE110P-1", "name" to "Introduction to Analog Integrated Circuits Design (Paired)", "units" to 3, "prerequisites" to listOf("CPE115-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE110P-2", "name" to "Introduction to Digital VLSI Design (Paired)", "units" to 3, "prerequisites" to listOf("ECE110P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE110P-3", "name" to "VLSI Test and Measurement (Paired)", "units" to 3, "prerequisites" to listOf("ECE110P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE154P" to listOf(
                mapOf("code" to "ECE154P-1", "name" to "ICT Infrastructure (Paired)", "units" to 3, "prerequisites" to listOf("ECE141-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE154P-2", "name" to "Electronics Auxiliary System (Paired)", "units" to 3, "prerequisites" to listOf("ECE107-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE152P" to listOf(
                mapOf("code" to "ECE152P-1", "name" to "Advanced Communication System and Design (Paired)", "units" to 3, "prerequisites" to listOf("ECE142-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE152P-2", "name" to "Advanced Networking (Paired)", "units" to 3, "prerequisites" to listOf("ECE143-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE152P-3", "name" to "Network Security (Paired)", "units" to 3, "prerequisites" to listOf("ECE144-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "SNAD175P" to listOf(
                mapOf("code" to "SNAD175P-1", "name" to "System and Network Administration 1 (Paired)", "units" to 3, "prerequisites" to listOf("ECE142-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SNAD175P-2", "name" to "System and Network Administration 2 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD175P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SNAD175P-3", "name" to "System and Network Administration 3 (Paired)", "units" to 3, "prerequisites" to listOf("SNAD175P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "NETA172P" to listOf(
                mapOf("code" to "NETA172P-2", "name" to "CCNA Routing and Switching 2 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "NETA172P-3", "name" to "CCNA Routing and Switching 3 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "NETA172P-4", "name" to "CCNA Routing and Switching 4 (Paired)", "units" to 3, "prerequisites" to listOf("NETA172P-2"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "AWS171P" to listOf(
                mapOf("code" to "AWS171P", "name" to "Cloud Foundations (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "AWS171P-1", "name" to "Cloud Development (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "AWS171P-2", "name" to "Cloud Architecture (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "AWS171P-3", "name" to "Cloud System Operations (Paired)", "units" to 3, "prerequisites" to listOf("AWS171P"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE194" to listOf(
                mapOf("code" to "ECE194", "name" to "Special Topics in Electronics Engineering (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "NETA173P" to listOf(
                mapOf("code" to "NETA173P", "name" to "HCIA Storage (Paired)", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE166P" to listOf(
                mapOf("code" to "ECE166P-1", "name" to "Semiconductor Material & Device Characterization (Paired)", "units" to 3, "prerequisites" to listOf("ECE102-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE166P-2", "name" to "Manufacturing and Process Control for Semiconductor and Electronics (Paired)", "units" to 3, "prerequisites" to listOf("ECE166P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE153" to listOf(
                mapOf("code" to "ECE153", "name" to "Fundamentals of Acoustics", "units" to 3, "prerequisites" to listOf("PHY035"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE153P-1", "name" to "Broadcast Production Engineering 1 (Paired)", "units" to 3, "prerequisites" to listOf("ECE141-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE153P-2", "name" to "Broadcast Production Engineering 2 (Paired)", "units" to 3, "prerequisites" to listOf("ECE153P-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE118P" to listOf(
                mapOf("code" to "ECE118P-1", "name" to "Advanced Power Supply Systems (Paired)", "units" to 3, "prerequisites" to listOf("ECE107-1", "ECE115"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE118P-2", "name" to "Renewable Energy Systems (Paired)", "units" to 3, "prerequisites" to listOf("ECE107-1", "ECE115"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE118P-3", "name" to "Motor Drives and Inverters (Paired)", "units" to 3, "prerequisites" to listOf("ECE107-1", "ECE115"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "ECE127" to listOf(
                mapOf("code" to "ECE127-1", "name" to "Fundamentals of Biomedical Engineering", "units" to 3, "prerequisites" to listOf("PHY034"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE127-2", "name" to "Medical Imaging", "units" to 3, "prerequisites" to listOf("PHY034"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ECE127-3", "name" to "Biophysical Phenomena", "units" to 3, "prerequisites" to listOf("PHY034"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "AENG" to listOf(
                mapOf("code" to "AENG012", "name" to "AB Power Engineering", "units" to 3, "prerequisites" to listOf("EE102-1"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "AENG013", "name" to "AB Electrification and Control Systems", "units" to 3, "prerequisites" to listOf("AENG012"), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            ),
            "GEN_ED" to listOf(
                mapOf("code" to "ENV075", "name" to "Environmental Science", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ENV076", "name" to "People and the Earth's Ecosystem", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "ENV078", "name" to "The Entrepreneurial Mind", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM080", "name" to "Philippine Popular Culture", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM081", "name" to "Indigenous Creative Crafts", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM082", "name" to "Reading Visual Art", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "HUM083", "name" to "Great Books", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SS084", "name" to "Religions, Religious Experiences and Spirituality", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4),
                mapOf("code" to "SS086", "name" to "Gender and Society", "units" to 3, "prerequisites" to listOf<String>(), "coRequisites" to listOf<String>(), "regularTerms" to listOf(1,2,3), "term" to 3, "yearLevel" to 4)
            )
        )
//        repository.uploadCourses("bsece_2022_2023", 1, "term_1", firstYearTerm1Courses) {
//            Log.d("UploadDebug", "First Year Term 1 courses uploaded")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 1, "term_2", firstYearTerm2Courses) {
//            Log.d("UploadDebug", "First Year Term 2 courses uploaded")
//        }
//        repository.uploadCourses("bsece_2022_2023", 1, "term_3", firstYearTerm3Courses) {
//            Log.d("UploadDebug", "First Year Term 3 courses uploaded")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 2, "term_1", secondYearTerm1Courses) {
//            Log.d("UploadDebug", "Second Year Term 1 courses uploaded")
//        }
//        repository.uploadCourses("bsece_2022_2023", 2, "term_2", secondYearTerm2Courses) {
//            Log.d("UploadDebug", "Second Year Term 2 courses uploaded")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 2, "term_3", secondYearTerm3Courses) {
//            Log.d("UploadDebug", "Second Year Term 3 courses uploaded")
//        }
//        repository.uploadCourses("bsece_2022_2023", 3, "term_1", thirdYearTerm1Courses) {
//            Log.d("UploadDebug", "Third Year Term 1 courses uploaded successfully!")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 3, "term_2", thirdYearTerm2Courses) {
//            Log.d("UploadDebug", "Third Year Term 2 courses uploaded successfully!")
//        }
        repository.uploadCourses("bsece_2022_2023", 3, "term_3", thirdYearTerm3Courses) {
            Log.d("UploadDebug", "Third Year Term 3 courses uploaded successfully!")
        }

//        repository.uploadCourses("bsece_2022_2023", 4, "term_1", fourthYearTerm1Courses) {
//            Log.d("UploadDebug", "Fourth Year Term 1 courses uploaded successfully!")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 4, "term_2", fourthYearTerm2Courses) {
//            Log.d("UploadDebug", "Fourth Year Term 2 courses uploaded successfully!")
//        }
//
//        repository.uploadCourses("bsece_2022_2023", 4, "term_3", fourthYearTerm3Courses) {
//            Log.d("UploadDebug", "Fourth Year Term 3 courses uploaded successfully!")
//        }
//
//        repository.uploadElectives("bsece_2022_2023", electives)


    }

    //-------------------------------------------------------------


















    // Duff added feb - 15
    fun updateCurriculumInFirestore(name: String, selectedCurriculum: String, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("students").document(userId)
            .update(
                "name", name,
                "curriculum", selectedCurriculum,
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

