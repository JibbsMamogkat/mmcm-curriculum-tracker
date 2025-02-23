package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var availableCourses by remember(studentId) { mutableStateOf(emptyList<Pair<CourseNode, String>>()) }
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet())
    var selectedTerm by remember { mutableStateOf(1) } // Default term
    val studentEmail by viewModel.studentEmail.observeAsState("Fetching Email...")

    // Fetch student data when studentId changes
    LaunchedEffect(studentId) {
        viewModel.fetchStudentData(studentId)
        availableCourses = emptyList()

        viewModel.loadStudentCompletedCourses(studentId) {
            availableCourses = viewModel.getAvailableCourses(studentId, selectedTerm)
        }
    }

    // Fetch available courses when the term changes
    LaunchedEffect(selectedTerm) {
        availableCourses = viewModel.getAvailableCourses(studentId, selectedTerm)
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
                text = "Available Courses for Student: $studentEmail",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TermFilterDropdown(selectedTerm) { newTerm ->
                selectedTerm = newTerm
            }

            if (availableCourses.isEmpty()) {
                Text(
                    "No available courses at this time.",
                    color = colorResource(id = R.color.mmcm_red)
                )
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