package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SectDarkBackground
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.MainGameScreen

class MainActivity : ComponentActivity() {
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SectDarkBackground
                ) {
                    MainGameScreen(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.resumeAudio()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.pauseAudio()
    }
}
