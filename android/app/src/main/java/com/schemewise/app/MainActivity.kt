package com.schemewise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.schemewise.app.ui.navigation.AppNavGraph
import com.schemewise.app.ui.theme.SchemeWiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.schemewise.app.util.DeadlineAlertEngine.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            SchemeWiseTheme {
                AppNavGraph()
            }
        }
    }
}
