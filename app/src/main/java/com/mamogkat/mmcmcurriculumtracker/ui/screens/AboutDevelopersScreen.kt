package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.navigation.Screen
import com.mamogkat.mmcmcurriculumtracker.ui.year.curriculumData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDevelopersScreen(navController: NavController) {
    ReusableScaffold(
        navController = navController,
        topBarTitle = "About the Developers"
    ){
        paddingValues ->
        AboutDevelopersScreenContent(paddingValues)
    }
}

@Preview
@Composable
fun AboutDevelopersScreenPreview() {
    AboutDevelopersScreen(rememberNavController())
}


@Composable
fun DeveloperItem(
    imageRes: Int,
    name: String,
    description: String
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Developer Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Developer Image",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .padding(8.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Name and Description
        Column(
            modifier = Modifier
                .padding(start = 16.dp),
        ){
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.mmcm_red)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = colorResource(id = R.color.mmcm_black)
            )
        }}

}


@Composable
fun AboutDevelopersScreenContent(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //Developer 1
        DeveloperItem(
            imageRes = R.drawable.jameel_image,
            name = "Mohammad Jameel Jibreel N. Mamogkat",
            description = """
                    BS Computer Engineering Student
                    Mapua Malayan Colleges Mindanao
                    Developer of MMCM Curriculum Tracker
                """.trimIndent()
        )


        //Developer 2
        DeveloperItem(
            imageRes = R.drawable.duff_image,
            name = "Duff S. Bastasa",
            description = """
                    BS Computer Engineering Student
                    Mapua Malayan Colleges Mindanao
                    Developer of MMCM Curriculum Tracker
                """.trimIndent()
        )

    }
}

