package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R

@Composable
fun NextCoursesScreen(navController: NavController) {
    ReusableScaffold(
        navController = navController,
        topBarTitle = "Next Courses..."
    ) {paddingValues ->
        NextCoursesContent(paddingValues)
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
fun NextCoursesContent(paddingValues: PaddingValues) {
    val courses = listOf(
        Course("CPE103-4", "Microprocessors", "CPE101-1", "None", "3.0"),
        Course("CPE103L-4", "Microprocessors (Laboratory)", "CPE101L-1", "CPE103-4", "1.0"),
        Course("CPE107-1", "Software Design", "CS105L", "None", "3.0"),
        Course("CPE107L-1", "Software Design (Laboratory)", "CS105L", "CPE107-1", "1.0"),
        Course("CPE143L", "Web Design and Development (Laboratory)", "CPE142L", "None", "2.0"),
        Course("ECE130", "Feedback and Control Systems", "MATH116", "None", "3.0"),
        Course("GEELEC02", "GE Elective 2", "None", "None", "3.0"),
        Course("TEC100", "Technopreneurship", "EMGT100", "None", "3.0"),
        Course("PE004", "Physical Activities Toward Health and Fitness 4", "PE001, PE002", "None", "2.0")
    )

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)) {

        FilterRow()

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(courses.size) { index ->
                CourseItem(course = courses[index])
                Divider()
            }
        }
    }
}

// Composable for the Filter Row
@Composable
fun FilterRow() {
    var selectedTerm by remember { mutableStateOf("1st Term") }
    val terms = listOf("1st Term", "2nd Term", "3rd Term")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filter:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 8.dp)
        )

        Box(modifier = Modifier.wrapContentSize()) {
            var expanded by remember { mutableStateOf(false) }
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    contentColor = colorResource(id = R.color.mmcm_white)
                )
            ) {
                Text(selectedTerm)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                terms.forEach { term ->
                    DropdownMenuItem(
                        text = { Text(term) },
                        onClick = {
                            selectedTerm = term
                            expanded = false
                        }
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