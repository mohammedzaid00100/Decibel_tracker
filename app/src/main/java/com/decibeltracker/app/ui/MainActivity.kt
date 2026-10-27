package com.decibeltracker.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.decibeltracker.app.ui.details.RecordingDetailsScreen
import com.decibeltracker.app.ui.details.RecordingDetailsViewModel
import com.decibeltracker.app.ui.history.HistoryScreen
import com.decibeltracker.app.ui.history.HistoryViewModel
import com.decibeltracker.app.ui.main.MainScreen
import com.decibeltracker.app.ui.main.MainViewModel
import com.decibeltracker.app.ui.splash.SplashScreen
import com.decibeltracker.app.ui.theme.CharcoalBg
import com.decibeltracker.app.ui.theme.DecibelTrackerTheme

sealed class AppScreen {
    object Splash : AppScreen()
    object Main : AppScreen()
    object History : AppScreen()
    data class Details(val recordingId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DecibelTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CharcoalBg
                ) {
                    DecibelTrackerAppContent(
                        mainViewModel = mainViewModel,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun DecibelTrackerAppContent(
    mainViewModel: MainViewModel,
    historyViewModel: HistoryViewModel
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }

    BackHandler(enabled = currentScreen !is AppScreen.Main && currentScreen !is AppScreen.Splash) {
        currentScreen = when (currentScreen) {
            is AppScreen.Details -> AppScreen.History
            is AppScreen.History -> AppScreen.Main
            else -> AppScreen.Main
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (initialState is AppScreen.Splash || targetState is AppScreen.Splash) {
                fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
            } else {
                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            is AppScreen.Splash -> {
                SplashScreen(
                    onSplashFinished = { currentScreen = AppScreen.Main }
                )
            }
            is AppScreen.Main -> {
                MainScreen(
                    viewModel = mainViewModel,
                    onNavigateToHistory = { currentScreen = AppScreen.History },
                    onOpenRecordingDetails = { id -> currentScreen = AppScreen.Details(id) }
                )
            }
            is AppScreen.History -> {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateBack = { currentScreen = AppScreen.Main },
                    onSelectRecording = { id -> currentScreen = AppScreen.Details(id) }
                )
            }
            is AppScreen.Details -> {
                val detailsViewModel = remember(screen.recordingId) {
                    RecordingDetailsViewModel(screen.recordingId)
                }
                RecordingDetailsScreen(
                    viewModel = detailsViewModel,
                    onNavigateBack = { currentScreen = AppScreen.History }
                )
            }
        }
    }
}
