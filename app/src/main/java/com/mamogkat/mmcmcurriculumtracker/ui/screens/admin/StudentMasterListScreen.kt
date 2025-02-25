package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
                    title = { Text(text = "Admin Dashboard - Student List", ) },
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
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorResource(id = R.color.mmcm_white))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
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
fun StudentCard(
    student: Student,
    curriculums: List<Curriculum>,
    viewModel: AdminViewModel,
    navController: NavController
) {
    val curriculumMap = viewModel.getCurriculumNameMap()
    var approvalExpanded by remember { mutableStateOf(false) }
    var curriculumExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    // Observe student-specific states from ViewModel
    val studentApprovalStatus by viewModel.studentApprovalStatus[student.studentID]?.let {
        mutableStateOf(it)
    } ?: remember { mutableStateOf("Fetching...") }

    val studentCurriculum by viewModel.studentCurriculum[student.studentID]?.let {
        mutableStateOf(it)
    } ?: remember { mutableStateOf("Fetching...") }

    val studentProgram by viewModel.studentProgram[student.studentID]?.let {
        mutableStateOf(it)
    } ?: remember { mutableStateOf("Fetching...") }

    // Fetch only once
    LaunchedEffect(student.studentID) {
        Log.d("StudentCard", "Fetching data for Student ID: ${student.studentID}")
        viewModel.fetchStudentApprovalStatus(student.studentID)
        viewModel.fetchStudentCurriculum(student.studentID)
        viewModel.fetchStudentProgram(student.studentID) // ✅ Fetch program from Firestore
    }


    Log.d("StudentCard", "Final UI values for student: ${student.email} - Curriculum: $studentCurriculum, Approval Status: $studentApprovalStatus, Program: $studentProgram")

    val curriculumName = curriculumMap[studentCurriculum] ?: "Not Assigned"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Email: ${student.email}", fontSize = 16.sp)
            Text(text = "Program: $studentProgram", fontSize = 16.sp, fontStyle = FontStyle.Italic) // ✅ Display program
            Text(text = "Current Curriculum: $curriculumName", fontSize = 16.sp, fontStyle = FontStyle.Italic)
            Text(text = "Approval Status: $studentApprovalStatus", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))


            // **✅ Program Dropdown**
            Box {
                Button(onClick = { programExpanded = true }) {
                    Text("Update Program")
                }
                DropdownMenu(
                    expanded = programExpanded,
                    onDismissRequest = { programExpanded = false }
                ) {
                    listOf("BS Computer Engineering", "BS Electronics and Communications Engineering", "BS Electrical Engineering").forEach { program ->
                        DropdownMenuItem(
                            text = { Text(program.capitalize()) },
                            onClick = {
                                Log.d("StudentCardBug", "Updating approval status for Student ID: ${student.studentID} to $program")
                                viewModel.updateStudentProgram(student.studentID, program) // ✅ Update Firestore
                                programExpanded = false
                            }
                        )
                    }
                }
            }

            // **✅ Approval Status Dropdown**
            Box {
                Button(onClick = { approvalExpanded = true }) {
                    Text("Update Approval Status")
                }

                DropdownMenu(
                    expanded = approvalExpanded,
                    onDismissRequest = { approvalExpanded = false }
                ) {
                    listOf("pending", "approved").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.capitalize()) },
                            onClick = {
                                Log.d("StudentCard", "Updating approval status for Student ID: ${student.studentID} to $status")
                                viewModel.updateStudentApprovalStatus(student.studentID, status) // ✅ Update Firestore
                                approvalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // **✅ Curriculum Dropdown**
            Box {
                Button(onClick = { curriculumExpanded = true }) {
                    Text("Update Curriculum")
                }

                DropdownMenu(
                    expanded = curriculumExpanded,
                    onDismissRequest = { curriculumExpanded = false }
                ) {
                    curriculums.forEach { curriculum ->
                        DropdownMenuItem(
                            text = { Text(curriculum.name) },
                            onClick = {
                                Log.d("StudentCard", "Updating curriculum for Student ID: ${student.studentID} to ${curriculum.curriculumID}")
                                viewModel.updateStudentCurriculum(student.studentID, curriculum.curriculumID)
                                curriculumExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // **✅ Navigation Buttons**
            Button(
                onClick = {
                    Log.d("StudentCard", "Navigating to Curriculum Details for Student ID: ${student.studentID}")
                    navController.navigate("admin_curriculum_overview/${student.studentID}")
                }
            ) {
                Text("View Curriculum Details")
            }
            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    Log.d("StudentCard", "Navigating to Next Available Courses for Student ID: ${student.studentID}")
                    navController.navigate("admin_next_available_courses_screen/${student.studentID}")
                },
                enabled = if (studentApprovalStatus == "approved") true else false
            ) {
                Text("Next Available Courses")
            }

            Spacer(modifier = Modifier.height(8.dp))
            var showDialog by remember { mutableStateOf(false) }
            // **✅ Remove Student Button**
            Button(
                onClick = { showDialog = true }, // Show confirmation dialog
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Remove Student", color = MaterialTheme.colorScheme.onError)
            }

            // Confirmation Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false }, // Close dialog if dismissed
                    title = { Text("Confirm Removal") },
                    text = { Text("Are you sure you want to remove this student? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                Log.d("StudentCard", "Removing Student ID: ${student.studentID}")
                                viewModel.removeStudent(student.studentID)
                                showDialog = false
                            }
                        ) {
                            Text("Yes, Remove")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}



