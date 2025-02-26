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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCurriculumOverviewScreen(
    navController: NavController,
    studentId: String,
    viewModel: CurriculumViewModel = viewModel()
) {
    Log.d("AdminCurriculumOverview", "Screen Loaded for studentId: $studentId")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val courseGraph by viewModel.courseGraph.observeAsState()
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet()) // ✅ Bind directly to Firestore

    // Fetch curriculum when screen loads
    LaunchedEffect(studentId) {
        Log.d("AdminCurriculumOverview", "Fetching student data for studentId: $studentId")
        viewModel.fetchStudentData(studentId)
    }

    // Wrap scaffold inside the ModalNavigationDrawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Curriculum Overview") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.mmcm_blue),
                        titleContentColor = colorResource(id = R.color.mmcm_white)
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorResource(id = R.color.mmcm_white))
                        }
                    })
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                if (courseGraph == null) {
                    Log.d("AdminCurriculumOverview", "Waiting for courseGraph data...")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.mmcm_blue))
                    }
                } else {
                    val groupedCourses = courseGraph!!.groupedCourses
                    val electiveCourses = courseGraph!!.electives

                    Log.d("AdminCurriculumOverview", "Grouped Courses for studentId: $studentId: $groupedCourses")
                    Log.d("AdminCurriculumOverview", "Elective Courses for studentId: $studentId: $electiveCourses")

                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        // 🔹 Iterate over Years
                        groupedCourses.toSortedMap(compareBy { it }) // Ensure years are sorted numerically
                            .forEach { (year, terms) ->
                                item {
                                    Text(
                                        text = "Year $year",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                    )
                                }
                                // 🔹 Iterate over Terms
                                terms.toSortedMap(compareBy { it }) // Sort terms numerically
                                    .forEach { (term, courses) ->
                                        item {
                                            Text(
                                                text = "Term $term",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                            )
                                        }
                                        // 🔹 Display courses for this term
                                        items(courses) { course ->
                                            CourseItem(
                                                courseName = course.name,
                                                courseCode = course.code,
                                                isChecked = completedCourses.contains(course.code), // ✅ Reflect real Firestore data
                                                onCheckChange = { isChecked ->
                                                    viewModel.updateCompletedCourses(studentId, course.code, isChecked) // ✅ Save instantly
                                                }
                                            )
                                        }
                                    }
                            }

                        // 🔹 Display Electives Section
                        if (electiveCourses.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Elective Courses",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                )
                            }
                            items(electiveCourses) { elective ->
                                CourseItem(
                                    courseName = elective.name,
                                    courseCode = elective.code,
                                    isChecked = completedCourses.contains(elective.code), // ✅ Reflect real Firestore data
                                    onCheckChange = { isChecked ->
                                        viewModel.updateCompletedCourses(studentId, elective.code, isChecked) // ✅ Save instantly
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CourseItem(courseName: String, courseCode: String, isChecked: Boolean, onCheckChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(courseName, modifier = Modifier.weight(1f))
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckChange(it) } // ✅ Correctly update state
        )
    }
}

