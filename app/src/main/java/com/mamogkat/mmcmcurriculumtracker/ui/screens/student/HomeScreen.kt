package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.mamogkat.mmcmcurriculumtracker.R

@Composable
fun StudentHomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.mmcm_white))
    ) {
        Text(
            text = "Student Home Screen!",
            style = MaterialTheme.typography.headlineSmall,
            color = colorResource(id = R.color.mmcm_red),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
