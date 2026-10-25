package com.example.studentexpensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.ui.navigation.AppNavigation
import com.example.studentexpensetracker.ui.theme.StudentExpenseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkModePref by userPreferencesRepository.isDarkMode.collectAsState(initial = null)
            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = isDarkModePref ?: isSystemDark

            StudentExpenseTrackerTheme(darkTheme = useDarkTheme) {
                AppNavigation()
            }
        }
    }
}
