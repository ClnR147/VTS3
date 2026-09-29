package com.example.vtsdaily3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.vtsdaily3.ui.AppRoot
import com.example.vtsdaily3.ui.theme.Vts3DailyTheme
import android.util.Log
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thread {
            try {
                val result = RoutesApiTest.testRouteMatrix()
                Log.d("ROUTES_TEST", result)
            } catch (ex: Exception) {
                Log.e("ROUTES_TEST", "Route test failed", ex)
            }
        }
        setContent {
            Vts3DailyTheme {
                AppRoot()
            }
        }
    }
}