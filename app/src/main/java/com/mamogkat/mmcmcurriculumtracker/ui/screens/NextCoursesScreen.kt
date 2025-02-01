package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

@Composable
fun NextCoursesScreen(navController: NavController) {
    ReusableScaffold(
        navController = navController,
        topBarTitle = "Next Courses..."
    ) {paddingValues ->
        NextCoursesContent(paddingValues, currentTerm = 1, completedCourses = emptySet())
    }
}

@Composable
fun CourseItem(course: Course) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "${course.code} - ${course.title}",
            color = colorResource(id = R.color.mmcm_red),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Units: ${course.units}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Prerequisite: ${course.prerequisite}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Co-requisite: ${course.corequisite}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class Course(
    val code: String,
    val title: String,
    val prerequisite: String,
    val corequisite: String,
    val units: String
)

@Composable
fun NextCoursesContent(
    paddingValues: PaddingValues,
    viewModel: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    currentTerm: Int,
    completedCourses: Set<String>
) {
    val availableCourses by remember{ mutableStateOf(viewModel.getAvailableCourses())}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Available Courses",
            style = MaterialTheme.typography.headlineMedium
        )
        LazyColumn {
            items(availableCourses) { (course, color) ->
                val backgroundColor = when (color) {
                    "green" -> Color(0xFF4CAF50) // Green
                    "orange" -> Color(0xFFFFA500) // Orange
                    "red" -> Color(0xFFD32F2F) // Red
                    else -> Color.White
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                        .background(backgroundColor)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${course.code} - ${course.name} (${course.units} units)",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun NextCoursesScreenPreview() {
    NextCoursesScreen(rememberNavController())
}