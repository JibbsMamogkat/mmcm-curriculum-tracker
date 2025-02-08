package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun AboutDevelopersScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.mmcm_white))
    ) {
        AboutDevelopersScreenContent(
            paddingValues = PaddingValues(16.dp),
        )
    }
}

@Preview
@Composable
fun AboutDevelopersScreenPreview() {
    AboutDevelopersScreen()
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.mmcm_red)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
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

