package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

@Composable
fun ManageCurriculumsPage(
    navController: NavController
){
    Column {
        UploadCPE2022Courses()
    }
}
@Composable
fun UploadCPE2022Courses(viewModel: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel()){
    val buttons = listOf(
        "Upload BS CPE 2022 First Year Term 1 Courses" to { viewModel.uploadFirstYearTerm1() },
        "Upload BS CPE 2022 First Year Term 2 Courses" to { viewModel.uploadFirstYearTerm2() },
        "Upload BS CPE 2022 First Year Term 3 Courses" to { viewModel.uploadFirstYearTerm3() },
        "Upload BS CPE 2022 Second Year Term 1 Courses" to { viewModel.uploadSecondYearTerm1() },
        "Upload BS CPE 2022 Second Year Term 2 Courses" to { viewModel.uploadSecondYearTerm2() },
        "Upload BS CPE 2022 Second Year Term 3 Courses" to { viewModel.uploadSecondYearTerm3() },
        "Upload BS CPE 2022 Third Year Term 1 Courses" to { viewModel.uploadThirdYearTerm1() },
        "Upload BS CPE 2022 Third Year Term 2 Courses" to { viewModel.uploadThirdYearTerm2() },
        "Upload BS CPE 2022 Third Year Term 3 Courses" to { viewModel.uploadThirdYearTerm3() },
        "Upload BS CPE 2022 Fourth Year Term 1 Courses" to { viewModel.uploadFourthYearTerm1() },
        "Upload BS CPE 2022 Fourth Year Term 2 Courses" to { viewModel.uploadFourthYearTerm2() },
        "Upload BS CPE 2022 Fourth Year Term 3 Courses" to { viewModel.uploadFourthYearTerm3() },
        "Upload BS CPE 2022 Elective Courses" to { viewModel.uploadElectives() },
        "Update Regular Terms in Firestore" to { viewModel.updateRegularTermsInFirestore() },
        "Update Regular Terms for Electives" to { viewModel.updateRegularTermsForElectives() }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(buttons) { (label, action) ->
            Button(
                onClick = action,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = label)
            }
        }
    }
}


