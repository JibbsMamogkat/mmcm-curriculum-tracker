package com.mamogkat.mmcmcurriculumtracker.ui.screens.student
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminNavigationDrawer
import com.mamogkat.mmcmcurriculumtracker.ui.year.curriculumData
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel


data class Curriculum(
    val code: String,
    val title: String,
    val lecHours: Double,
    val labHours: Double,
    val units: Double,
    val prerequisites: String?,
    val coRequisites: String?,
    var isChecked: Boolean = false
)
@Composable
fun CurriculumOverviewScreen(
    studentId: String,
    viewModel: CurriculumViewModel = viewModel()
) {
    val courseGraph by viewModel.courseGraph.observeAsState()
    val completedCourses by viewModel.completedCourses.observeAsState(emptySet())
    val curriculumName by viewModel.curriculumName.observeAsState("YOUR CURRICULUM")

    val expandedYears by viewModel.expandedYears.collectAsState()
    val expandedTerms by viewModel.expandedTerms.collectAsState()
    val expandedElectives by viewModel.expandedElectives.collectAsState()

    val scrollPosition by viewModel.scrollPosition.collectAsState()
    val scrollOffset by viewModel.scrollOffset.collectAsState()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollPosition,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )

    LaunchedEffect(studentId) {
        viewModel.fetchStudentData(studentId)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.saveScrollPosition(index, offset)
            }
    }

    Surface(color = colorResource(id = R.color.mmcm_white)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = curriculumName,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (courseGraph == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.mmcm_blue))
                }
            } else {
                val groupedCourses = courseGraph!!.groupedCourses
                val electiveCourses = courseGraph!!.electives

                LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                    groupedCourses.toSortedMap(compareBy { it })
                        .forEach { (year, terms) ->
                            val expandedYear = expandedYears[year] ?: false

                            // 🔹 Year Clickable Header
                            item {
                                val allTermsCompleted = terms.all { (_, courses) ->
                                    courses.all { completedCourses.contains(it.code) }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (expandedYear) colorResource(id = R.color.mmcm_blue) else Color.Transparent)
                                        .clickable { viewModel.toggleYearExpansion(year) }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Year $year",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = if (allTermsCompleted) Color.Green else Color.White.takeIf { expandedYear }
                                            ?: colorResource(id = R.color.mmcm_blue),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Icon(
                                        painter = if (expandedYear)
                                            painterResource(id = R.drawable.baseline_expand_less_24)
                                        else
                                            painterResource(id = R.drawable.baseline_expand_more_24),
                                        tint = Color.White.takeIf { expandedYear } ?: Color.Black,
                                        contentDescription = if (expandedYear) "Collapse" else "Expand"
                                    )
                                }
                            }

                            // 🔹 Only Show Terms If Year is Expanded
                            if (expandedYear) {
                                terms.toSortedMap(compareBy { it })
                                    .forEach { (term, courses) ->
                                        val allCompleted = courses.all { completedCourses.contains(it.code) }
                                        val expandedTerm = expandedTerms[Pair(year, term)] ?: !allCompleted

                                        // 🔹 Term Clickable Header
                                        item {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(if (expandedTerm) Color.LightGray else Color.Transparent)
                                                    .clickable { viewModel.toggleTermExpansion(year, term, allCompleted) }
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.Center, // ✅ Center text
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Term $term",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 18.sp,
                                                    color = if (allCompleted) Color.Green else Color.Red, // ✅ Green for completed, Red for incomplete
                                                    textAlign = TextAlign.Center // ✅ Ensure text is centered
                                                )
                                                Icon(
                                                    painter = if (expandedTerm)
                                                        painterResource(id = R.drawable.baseline_expand_less_24)
                                                    else
                                                        painterResource(id = R.drawable.baseline_expand_more_24),
                                                    contentDescription = if (expandedTerm) "Collapse" else "Expand"
                                                )
                                            }
                                        }

                                        // 🔹 Show Courses If Term is Expanded
                                        if (expandedTerm) {
                                            items(courses) { course ->
                                                CourseItem(
                                                    courseName = course.name,
                                                    courseCode = course.code,
                                                    isCompleted = completedCourses.contains(course.code)
                                                )
                                            }
                                        }
                                    }
                            }
                        }

                    // 🔹 Expandable Elective Courses Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleElectivesExpansion() }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Elective Courses",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = colorResource(id = R.color.mmcm_blue),
                                modifier = Modifier.padding(8.dp)
                            )
                            Icon(
                                painter = if (expandedElectives)
                                    painterResource(id = R.drawable.baseline_expand_less_24)
                                else
                                    painterResource(id = R.drawable.baseline_expand_more_24),
                                contentDescription = if (expandedElectives) "Collapse" else "Expand"
                            )
                        }
                    }

                    if (expandedElectives) {
                        items(electiveCourses) { elective ->
                            CourseItem(
                                courseName = elective.name,
                                courseCode = elective.code,
                                isCompleted = completedCourses.contains(elective.code)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CourseItem(courseName: String, courseCode: String, isCompleted: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) { // Let text take most space
                Text(courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(courseCode, fontSize = 14.sp, color = Color.Gray)
            }
            if (isCompleted) {
                Box(
                    modifier = Modifier.width(100.dp), // Fixed width prevents line breaks
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Completed",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



