package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

@Composable
fun UploadCoursesScreen(
    navController: NavController
){
    Column {
        UploadCPE2022Courses()
    }
}
@Composable
fun UploadCPE2022Courses(viewModel: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel()){
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        modifier = androidx.compose.ui.Modifier.padding(16.dp)
    ) {
        Button(onClick = { viewModel.uploadFirstYearTerm1() }) {
            Text(text = "Upload BS CPE 2022 First Year Term 1 Courses")
        }
        Button(onClick = { viewModel.uploadFirstYearTerm2() }) {
            Text(text = "Upload BS CPE 2022 First Year Term 2 Courses")
        }
    }
}

@Preview
@Composable
fun PreviewUploadCoursesScreen(){
    UploadCoursesScreen(navController = rememberNavController())
}
