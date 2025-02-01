package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(onClick = { viewModel.uploadFirstYearTerm1() }) {
            Text(text = "Upload BS CPE 2022 First Year Term 1 Courses")
        }
        Button(onClick = { viewModel.uploadFirstYearTerm2() }) {
            Text(text = "Upload BS CPE 2022 First Year Term 2 Courses")
        }
        Button(onClick = { viewModel.uploadFirstYearTerm3()}) {
            Text(text = "Upload BS CPE 2022 First Year Term 3 Courses")
        }
        Button(onClick = { viewModel.uploadSecondYearTerm1() }) {
            Text(text = "Upload BS CPE 2022 Second Year Term 1 Courses")
        }
        Button(onClick = { viewModel.uploadSecondYearTerm2() }) {
            Text(text = "Upload BS CPE 2022 Second Year Term 2 Courses")
        }
        Button(onClick = { viewModel.uploadSecondYearTerm3() }) {
            Text(text = "Upload BS CPE 2022 Second Year Term 3 Courses")
        }
        Button(onClick = { viewModel.uploadThirdYearTerm1() }) {
            Text(text = "Upload BS CPE 2022 Third Year Term 1 Courses")
        }
        Button(onClick = { viewModel.uploadThirdYearTerm2() }) {
            Text(text = "Upload BS CPE 2022 Third Year Term 2 Courses")
        }
        Button(onClick = { viewModel.uploadThirdYearTerm3() }) {
            Text(text = "Upload BS CPE 2022 Third Year Term 3 Courses")
        }
        Button(onClick = { viewModel.uploadFourthYearTerm1() }) {
            Text(text = "Upload BS CPE 2022 Fourth Year Term 1 Courses")
        }
        Button(onClick = { viewModel.uploadFourthYearTerm2() }) {
            Text(text = "Upload BS CPE 2022 Fourth Year Term 2 Courses")
        }
        Button(onClick = { viewModel.uploadFourthYearTerm3() }) {
            Text(text = "Upload BS CPE 2022 Fourth Year Term 3 Courses")
        }
        Button(onClick = { viewModel.uploadElectives() }) {
            Text(text = "Upload BS CPE 2022 Elective Courses")
        }

    }
}

@Preview
@Composable
fun PreviewUploadCoursesScreen(){
    UploadCoursesScreen(navController = rememberNavController())
}
