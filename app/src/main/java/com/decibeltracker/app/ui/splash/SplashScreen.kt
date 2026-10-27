package com.decibeltracker.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.R
import com.decibeltracker.app.ui.theme.BrandTurquoise
import kotlinx.coroutines.delay

private val SplashCanvasBg = Color(0xFFD9D9D9) // Exact matching background of the official logo artwork
private val LogoCrimson = Color(0xFF3D0000)   // Exact deep crimson of the logo speedometer needle/pivot

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        delay(800)
        onSplashFinished()
    }

    Scaffold(
        containerColor = SplashCanvasBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value)
                    .padding(horizontal = 32.dp)
            ) {
                // Official Decibel Tracker Logo displayed proportionally
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_splash),
                    contentDescription = "Decibel Tracker Official Logo",
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "DECIBEL TRACKER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    color = LogoCrimson
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Precision Acoustic Sound Meter",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A4A4A),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                LinearProgressIndicator(
                    modifier = Modifier
                        .width(140.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = BrandTurquoise,
                    trackColor = Color(0xFFBEBEBE)
                )
            }
        }
    }
}
