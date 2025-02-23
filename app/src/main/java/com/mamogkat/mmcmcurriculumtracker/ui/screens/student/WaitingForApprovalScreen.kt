package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import coil.decode.GifDecoder
import coil.ImageLoader
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RunningGif
import com.mamogkat.mmcmcurriculumtracker.R


@Composable
fun WaitingForApprovalScreen(context: Context) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(id = R.color.mmcm_white)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                RunningGif(context)

                Text(
                    text = "Waiting for Admin Approval",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.mmcm_red),
                    fontFamily = FontFamily.SansSerif, // Improved readability
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp)) // Added spacing

                Text(
                    text = "Sit back and relax while the admin reviews your curriculum. This process ensures that your academic progress is accurately verified and aligned with program requirements.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.mmcm_blue),
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Justify, // Justified text
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                )
            }
        }
    }
}

