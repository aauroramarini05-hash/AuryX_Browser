package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.browser.ui.AuryxMainScreen
import com.example.browser.ui.BrowserViewModel
import com.example.browser.ui.ThemeManager
import com.example.ui.theme.Typography

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by viewModel.currentTheme.collectAsState()
            val colorScheme = ThemeManager.getColorScheme(theme)

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    AuryxMainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
