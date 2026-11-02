package com.sydney.sydney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sydney.sydney.navigation.AppNavHost
import com.sydney.sydney.ui.theme.ColFindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColFindTheme {
                AppNavHost()
            }
        }
    }
}
