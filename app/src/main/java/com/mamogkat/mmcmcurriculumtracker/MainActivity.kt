    package com.mamogkat.mmcmcurriculumtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.mamogkat.mmcmcurriculumtracker.navigation.AppNavHost
import com.mamogkat.mmcmcurriculumtracker.ui.theme.MMCMCurriculumTrackerTheme
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect


    class MainActivity : ComponentActivity() {
        private val authViewModel: AuthViewModel by viewModels()
        private val curriculumViewModel: CurriculumViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // 🔥 Run the upload function once
//      curriculumViewModel.uploadBSEE_2024_2025()
        setContent {
            MMCMCurriculumTrackerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    LaunchedEffect(Unit) {
                        authViewModel.checkUserLogin(navController)
                    }
                    AppNavHost(navController = navController, adminViewModel = AdminViewModel(), curriculumViewModel = CurriculumViewModel(), authViewModel = authViewModel)

                    FirebaseApp.initializeApp(this)
                }
            }
        }
    }
}
