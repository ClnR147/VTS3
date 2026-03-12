package com.example.vtsdaily3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.vtsdaily3.ui.AppRoot
import com.example.vtsdaily3.ui.theme.Vts3DailyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Vts3DailyTheme {
                AppRoot()
            }
        }
    }
}