package com.gymtracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gymtracker.app.ui.navigation.GymTrackerNavigation
import com.gymtracker.app.ui.theme.Background
import com.gymtracker.app.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as GymTrackerApp
        
        setContent {
            GymTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    GymTrackerNavigation(repository = app.repository)
                }
            }
        }
    }
}
