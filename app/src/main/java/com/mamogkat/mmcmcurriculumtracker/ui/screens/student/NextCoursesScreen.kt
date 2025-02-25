package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import android.util.Log
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.models.CourseNode
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.CourseItem
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.TermFilterDropdown
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel


@Composable
fun NextCoursesScreen(
    studentId: String,
    viewModel: CurriculumViewModel = viewModel()
) {
    Log.d("NextCoursesScreen", "Initializing screen for Student ID: $studentId")

    var selectedTerm by remember { mutableStateOf(1) } // Default term
    val studentEmail by viewModel.studentEmail.observeAsState("Fetching Email...")
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet())

    LaunchedEffect(studentId) {
        Log.d("NextCoursesScreen", "Listening to real-time updates for Student ID: $studentId")
        viewModel.fetchStudentData(studentId)
        viewModel.observeStudentData(studentId) {
            Log.d("NextCoursesScreen", "Real-time listener completed")
        }
    }

    val availableCourses by viewModel.availableCourses.collectAsStateWithLifecycle()

    LaunchedEffect(studentId, selectedTerm) {
        Log.d("NextCoursesScreen", "Fetching available courses for Student ID: $studentId, term: $selectedTerm")
        viewModel.getAvailableCoursesStudent(studentId, selectedTerm)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.mmcm_white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /*Text(
                text = "Available Courses: $studentEmail",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
*/
            TermFilterDropdown(selectedTerm) { newTerm ->
                selectedTerm = newTerm
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (availableCourses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.mmcm_red))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCourses) { (course, color) ->
                        CourseItem(course, color)
                    }
                }
            }
        }
    }
}
