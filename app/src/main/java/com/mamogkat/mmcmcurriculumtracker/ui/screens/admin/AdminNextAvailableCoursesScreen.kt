package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.navigation.Screen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNextAvailableCoursesScreen(
    studentId: String,
    navController: NavController,
    viewModel: CurriculumViewModel = viewModel()
) {
    Log.d("AdminNextAvailableCoursesScreen", "Initializing screen for Student ID: $studentId")

    val availableCourses by remember { mutableStateOf(emptyList<Pair<CourseNode, String>>()) }
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet())
    val enrolledTerm by viewModel.enrolledTerm.observeAsState(1)

    Log.d("AdminNextAvailableCoursesScreen", "Fetching available courses for Student ID: $studentId")

    // Fetch data when screen loads
    LaunchedEffect(studentId) {
        Log.d("AdminNextAvailableCoursesScreen", "LaunchedEffect triggered for Student ID: $studentId")
        viewModel.fetchStudentData(studentId) // Fetch curriculum and completed courses
        Log.d("AdminNextAvailableCoursesScreen", "Completed fetching student data for Student ID: $studentId")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Next Available Courses") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("AdminNextAvailableCoursesScreen", "Back button clicked")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                text = "Available Courses for Student: $studentId",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Fetch and display available courses
            Log.d("AdminNextAvailableCoursesScreen", "Fetching available courses for term: $enrolledTerm")
            val courses = viewModel.getAvailableCourses()
            Log.d("AdminNextAvailableCoursesScreen", "Available courses fetched: ${courses.size}")

            if (courses.isEmpty()) {
                Log.w("AdminNextAvailableCoursesScreen", "No available courses found for student $studentId")
                Text("No available courses at this time.", color = colorResource(id = R.color.mmcm_red))
            } else {
                LazyColumn {
                    items(courses) { (course, color) ->
                        Log.d(
                            "AdminNextAvailableCoursesScreen",
                            "Displaying course: ${course.name} (Code: ${course.code}), Color: $color"
                        )
                        CourseItem(course, color)
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: CourseNode, colorCode: String) {
    Log.d("CourseItem", "Rendering course: ${course.name} (Code: ${course.code}), Color: $colorCode")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (colorCode) {
                "green" -> Color(0xFF81C784).also { Log.d("CourseItem", "✅ Course offered this term") }
                "orange" -> Color(0xFFFFB74D).also { Log.d("CourseItem", "⚠️ Course available but not regularly offered") }
                "blue" -> Color(0xFF64B5F6).also { Log.d("CourseItem", "🔹 Elective course") }
                else -> Color(0xFFE57373).also { Log.e("CourseItem", "🚨 Unexpected course color classification!") }
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
