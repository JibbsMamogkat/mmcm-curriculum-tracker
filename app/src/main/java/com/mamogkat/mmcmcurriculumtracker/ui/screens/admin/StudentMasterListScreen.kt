package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.models.Curriculum
import com.mamogkat.mmcmcurriculumtracker.models.Student
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMasterListScreen(viewModel: AdminViewModel, navController: NavController) {
    val students by viewModel.studentList.observeAsState(emptyList())
    val curriculums by viewModel.curriculumList.observeAsState(emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Log.d("StudentMasterListScreen", "Fetching students and curriculums")
        viewModel.fetchStudents()
        viewModel.fetchCurriculums()
    }

    Log.d("StudentMasterListScreen", "Total students: ${students.size}")
    Log.d("StudentMasterListScreen", "Total curriculums: ${curriculums.size}")

    students.forEach { student ->
        Log.d("StudentMasterListScreen", "Student ID: ${student.studentID}, Name: ${student.name}")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Admin Dashboard", ) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.mmcm_blue),
                        titleContentColor = colorResource(id = R.color.mmcm_white)
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
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
    }
}

@Composable
fun StudentCard(student: Student, curriculums: List<Curriculum>, viewModel: AdminViewModel, navController: NavController) {
    val curriculumMap = viewModel.getCurriculumNameMap()
    var selectedCurriculum by remember { mutableStateOf(student.curriculum ?: "") }
    var curriculumName by remember { mutableStateOf(curriculumMap[selectedCurriculum] ?: "Not Assigned") }
    var expanded by remember { mutableStateOf(false) }

    Log.d("StudentCard", "Displaying student: ${student.name} with curriculum: ${student.curriculum}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${student.email}", fontSize = 16.sp)
            Text(text = "Current Curriculum: $curriculumName", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Curriculum Dropdown
            Box {
                Button(onClick = { expanded = true }) {
                    Text("Update Curriculum")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    curriculums.forEach { curriculum ->
                        DropdownMenuItem(
                            text = { Text(curriculum.name) },
                            onClick = {
                                selectedCurriculum = curriculum.curriculumID
                                curriculumName = curriculum.name // Update UI instantly
                                expanded = false
                                Log.d("StudentCard", "Updating student ${student.studentID} with curriculum: $selectedCurriculum")
                                viewModel.updateStudentCurriculum(student.studentID, selectedCurriculum)

                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons

            Button(onClick = { navController.navigate("admin_curriculum_overview/${student.studentID}") }) {
                Text("View Curriculum Details")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("admin_next_available_courses_screen/${student.studentID}") }) {
                Text("Next Available Courses")
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Remove Student Button
            Button(
                onClick = { viewModel.removeStudent(student.studentID) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove Student", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}

