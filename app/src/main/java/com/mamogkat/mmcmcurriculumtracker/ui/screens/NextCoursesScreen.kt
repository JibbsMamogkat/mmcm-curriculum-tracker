package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R

@Composable
fun NextCoursesScreen(navController: NavController) {
    ReusableScaffold(
        navController = navController,
        topBarTitle = "Next Courses..."
    ) {paddingValues ->
        NextCoursesContent()
    }
}

@Composable
fun CourseItem(course: Course) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "${course.code} - ${course.title}", fontSize = 18.sp, style = MaterialTheme.typography.headlineSmall)
        Text(text = "Prerequisite: ${course.prerequisite}", fontSize = 14.sp)
        Text(text = "Co-requisite: ${course.corequisite}", fontSize = 14.sp)
    }
}

data class Course(
    val code: String,
    val title: String,
    val prerequisite: String,
    val corequisite: String
)

@Composable
fun NextCoursesContent() {
    val courses = listOf(
        Course("CPE103-4", "Microprocessors", "CPE101-1", "None"),
        Course("CPE103L-4", "Microprocessors (Laboratory)", "CPE101L-1", "CPE103-4"),
        Course("CPE107-1", "Software Design", "CS105L", "None"),
        Course("CPE107L-1", "Software Design (Laboratory)", "CS105L", "CPE107-1"),
        Course("CPE143L", "Web Design and Development (Laboratory)", "CPE142L", "None"),
        Course("ECE130", "Feedback and Control Systems", "MATH116", "None"),
        Course("GEELEC02", "GE Elective 2", "None", "None"),
        Course("TEC100", "Technopreneurship", "EMGT100", "None"),
        Course("PE004", "Physical Activities Toward Health and Fitness 4", "PE001, PE002", "None")
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "Next Courses",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(courses.size) { index ->
                CourseItem(course = courses[index])
                Divider()
            }
        }
    }
}

@Preview
@Composable
fun NextCoursesScreenPreview() {
    NextCoursesScreen(rememberNavController())
}