package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAboutDevs(
    navController: NavController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "About the Developers",) },
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
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = colorResource(id = R.color.mmcm_white)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.mmcm_white))
            ) {
                AboutDevelopersScreenContent(
                    paddingValues
                )
            }
        }
    }
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

