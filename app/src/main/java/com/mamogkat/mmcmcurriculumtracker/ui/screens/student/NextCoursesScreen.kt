package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import android.util.Log
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.CourseItem
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.TermFilterDropdown
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextCoursesScreen(
    studentId: String,
    viewModel: CurriculumViewModel = viewModel()
) {
    Log.d("StudentNextAvailableCoursesScreen", "Initializing screen for Student ID: $studentId")

    var selectedTerm by remember { mutableStateOf(1) } // Default term
    val studentEmail by viewModel.studentEmail.observeAsState("Fetching Email...")
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet())

    LaunchedEffect(studentId) {
        Log.d("StudentNextAvailableCoursesScreen", "Listening to real-time updates for Student ID: $studentId")
        viewModel.fetchStudentData(studentId)
        viewModel.observeStudentData(studentId) {
            Log.d("StudentNextAvailableCoursesScreen", "Real-time listener completed")
        }
    }


    // Using Flow with collectAsStateWithLifecycle
    val availableCourses by viewModel.availableCourses.collectAsStateWithLifecycle()

    LaunchedEffect(studentId, selectedTerm) {
        Log.d("StudentNextAvailableCoursesScreen", "Fetching available courses for Student ID: $studentId, term: $selectedTerm")
        viewModel.getAvailableCoursesStudent(studentId, selectedTerm)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Next Available Courses") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    titleContentColor = colorResource(id = R.color.mmcm_white)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Available Courses: $studentEmail",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TermFilterDropdown(selectedTerm) { newTerm ->
                selectedTerm = newTerm
            }

            if (availableCourses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.mmcm_red))
                }
            } else {
                LazyColumn {
                    items(availableCourses) { (course, color) ->
                        CourseItem(course, color)
                    }
                }
            }
        }
    }
}


@Composable
fun CourseItem(course: CourseNode, colorCode: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (colorCode) {
                "green" -> Color(0xFF81C784)
                "orange" -> Color(0xFFFFB74D)
                "blue" -> Color(0xFF64B5F6)
                else -> Color(0xFFE57373)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = course.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Course Code: ${course.code}", color = Color.White)
            }
        }
    }
}

@Composable
fun TermFilterDropdown(selectedTerm: Int, onTermSelected: (Int) -> Unit) {
    val terms = listOf(1, 2, 3)
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text("Enrolling in Term: $selectedTerm")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            terms.forEach { term ->
                DropdownMenuItem(
                    text = { Text("Term $term") },
                    onClick = {
                        onTermSelected(term)
                        expanded = false
                    }
                )
            }
        }
    }
}