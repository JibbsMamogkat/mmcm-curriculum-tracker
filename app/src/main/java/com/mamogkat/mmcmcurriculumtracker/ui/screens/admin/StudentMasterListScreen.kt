package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.models.Curriculum
import com.mamogkat.mmcmcurriculumtracker.models.Student
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel

@Composable
fun StudentMasterListScreen(viewModel: AdminViewModel, navController: NavController) {
    val students by viewModel.studentList.observeAsState(emptyList())
    val curriculums by viewModel.curriculumList.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        Log.d("StudentMasterListScreen", "Fetching students and curriculums")
        viewModel.fetchStudents()
        viewModel.fetchCurriculums()
    }

    Log.d("StudentMasterListScreen", "Total students: ${students.size}")
    Log.d("StudentMasterListScreen", "Total curriculums: ${curriculums.size}")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Admin Dashboard - Student List", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (students.isEmpty()) {
            Text("No students found", modifier = Modifier.padding(top = 16.dp))
            Log.d("StudentMasterListScreen", "No students found")
        } else {
            LazyColumn {
                items(students) { student ->
                    Log.d("StudentMasterListScreen", "Displaying student: ${student.name}")
                    StudentCard(student, curriculums, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun StudentCard(student: Student, curriculums: List<Curriculum>, viewModel: AdminViewModel, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCurriculum by remember { mutableStateOf(student.curriculum ?: "") }

    Log.d("StudentCard", "Rendering card for: ${student.name}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${student.email}", fontSize = 16.sp)
            Text(text = "Current Curriculum: ${student.curriculum ?: "Not Assigned"}", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Curriculum Dropdown
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                curriculums.forEach { curriculum ->
                    DropdownMenuItem(
                        text = { Text(curriculum.name) },
                        onClick = {
                        selectedCurriculum = curriculum.curriculumID
                        Log.d("StudentCard", "Updating curriculum for ${student.name} to ${selectedCurriculum}")
                        viewModel.updateStudentCurriculum(student.studentID, selectedCurriculum)
                        expanded = false
                    })
                }
            }

            Button(onClick = {
                expanded = true
                Log.d("StudentCard", "Opening curriculum dropdown for ${student.name}")
            }) {
                Text("Update Curriculum")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons
            Button(onClick = {
                Log.d("StudentCard", "Navigating to curriculum details for ${student.name}")
                navController.navigate("curriculumDetail/${student.studentID}")
            }) {
                Text("View Curriculum Details")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                Log.d("StudentCard", "Navigating to next available courses for ${student.name}")
                navController.navigate("nextAvailableCourses/${student.studentID}")
            }) {
                Text("Next Available Courses")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Remove Student Button
            Button(
                onClick = {
                    Log.d("StudentCard", "Removing student: ${student.name}")
                    viewModel.removeStudent(student.studentID) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove Student", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}