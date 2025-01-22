package com.mamogkat.mmcmcurriculumtracker.ui.screens
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.year.curriculumData

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurriculumOverviewScreen(navController: NavController) {
    // State to track which term is expanded
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    // State to track checkbox selections
    val checkboxStates = remember {
        mutableStateMapOf<String, Boolean>()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Curriculum - 2022 - 2023") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    titleContentColor = colorResource(id = R.color.mmcm_white)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colorResource(id = R.color.mmcm_white))
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                backgroundColor = colorResource(id = R.color.mmcm_blue),
                contentColor = colorResource(id = R.color.mmcm_white)
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Home", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Curriculum", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("curriculum_overview") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tracker", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Tracker", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("next_courses") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Settings", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Account", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    onClick = { /* Handle Settings Navigation */ }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("About", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    onClick = { navController.navigate("about_developers") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            curriculumData.forEach { (year, terms) ->
                // Centered Year Title
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                terms.forEach { (term, courses) ->
                    item {
                        CollapsibleTerm(
                            term = term,
                            courses = courses,
                            isExpanded = expandedStates[term] ?: false,
                            onExpandChange = { expandedStates[term] = it },
                            checkboxStates = checkboxStates
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun CurriculumItem(
    curriculum: Curriculum,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (curriculum.isChecked) Color(0xFFDFFFDF) else Color.White
    val cardElevation = if (curriculum.isChecked) 8.dp else 4.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                CurriculumRow(label = "CODE:", value = curriculum.code)
                CurriculumRow(label = "TITLE:", value = curriculum.title)
                CurriculumRow(label = "LECTURE HOURS:", value = curriculum.lecHours.toString())
                CurriculumRow(label = "LAB HOURS:", value = curriculum.labHours.toString())
                CurriculumRow(label = "UNITS:", value = curriculum.units.toString())
                CurriculumRow(label = "PREREQUISITES:", value = curriculum.prerequisites ?: "None")
                CurriculumRow(label = "CO-REQUISITES:", value = curriculum.coRequisites ?: "None")
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (curriculum.isChecked) "Finish" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Green,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Checkbox(
                    checked = curriculum.isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    }
}

@Composable
fun CurriculumRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(150.dp) // Fixed width for labels to align properly
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CollapsibleTerm(
    term: String,
    courses: List<Curriculum>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    checkboxStates: MutableMap<String, Boolean>
) {
    Column {
        // Term Header (Clickable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandChange(!isExpanded) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = term,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { onExpandChange(!isExpanded) }) {
                Text(text = if (isExpanded) "Show Less" else "Show More")
            }
        }

        // Collapsible Content
        if (isExpanded) {
            courses.forEach { course ->
                val checkboxKey = "${term}_${course.code}"
                CurriculumItem(
                    curriculum = course.copy(isChecked = checkboxStates[checkboxKey] ?: false),
                    onCheckedChange = { isChecked ->
                        checkboxStates[checkboxKey] = isChecked // Persist checkbox state
                    }
                )
            }
        }
    }
}
@Preview(showBackground = false)
@Composable
fun CurriculumItemPreview() {
    // Create a mutable state to hold the checked state of the checkbox
    var isChecked by remember { mutableStateOf(false) }
    val sampleCurriculum = Curriculum(
        code = "CHM031",
        title = "Chemistry for Engineers",
        lecHours = 4.5,
        labHours = 0.0,
        units = 3.0,
        prerequisites = null,
        coRequisites = null,
        isChecked = isChecked // use the state here
    )

    CurriculumItem(
        curriculum = sampleCurriculum,
        onCheckedChange = { newCheckedState ->
            isChecked = newCheckedState
        }
    )
}

