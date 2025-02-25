package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.navigation.Screen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNextAvailableCoursesScreen(
    studentId: String,
    navController: NavController,
    viewModel: CurriculumViewModel = viewModel()
) {
    Log.d("AdminNextAvailableCoursesScreen", "Initializing screen for Student ID: $studentId")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var selectedTerm by remember { mutableStateOf(1) } // Default term
    val studentEmail by viewModel.studentEmail.observeAsState("Fetching Email...")
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet()) // ✅ Bind directly to Firestore


    // ✅ Ensure `completedCourses` is loaded first, then `availableCourses` updates
    LaunchedEffect(studentId) {
        Log.d("AdminNextAvailableCoursesScreen", "Fetching student data for Student ID: $studentId")
        viewModel.fetchStudentData(studentId)
        viewModel.observeStudentData(studentId) {
            Log.d("AdminNextAvailableCoursesScreen", "Completed courses loaded for Student ID: $studentId")
        }
    }
    // ✅ **Automatically recompute available courses when completedCourses updates**
    // Using Flow with collectAsStateWithLifecycle
    val availableCourses by viewModel.availableCourses.collectAsStateWithLifecycle()
    LaunchedEffect(studentId, selectedTerm) {
        Log.d("AdminNextAvailableCoursesScreen", "🔄 Fetching available courses for Student ID: $studentId, term: $selectedTerm")
        viewModel.getAvailableCoursesStudent(studentId, selectedTerm)
    }

    Log.d("AdminNextAvailableCoursesScreen", "Available courses computed for Student ID: $studentId")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Next Available Courses") },
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

                // 🔹 Term Filter Dropdown
                Log.d("AdminNextAvailableCoursesScreen", "Rendering term filter dropdown")
                TermFilterDropdown(selectedTerm) { newTerm ->
                    selectedTerm = newTerm
                }

                // 🔹 Display available courses
                if (availableCourses.isEmpty()) {
                    Log.w("AdminNextAvailableCoursesScreen", "No available courses found for student $studentId")
                    Text(
                        "Fetching....",
                        color = colorResource(id = R.color.mmcm_red)
                    )
                } else {
                    Log.d("AdminNextAvailableCoursesScreen", "Found ${availableCourses.size} available courses for student $studentId")
                    LazyColumn {
                        items(availableCourses) { (course, color) ->
                            CourseItem(course, color)
                        }
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
                    text = { Text("Term $term") }, // Explicitly pass text here
                    onClick = {
                        onTermSelected(term)  // Updates selectedTerm in the parent composable
                        Log.d("TermFilterDropdown", "User selected term: $term")
                        expanded = false
                    }
                )

            }
        }
    }
}

