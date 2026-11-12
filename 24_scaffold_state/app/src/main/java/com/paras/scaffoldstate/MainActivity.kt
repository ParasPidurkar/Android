package com.paras.scaffoldstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paras.scaffoldstate.ui.theme.ScaffoldStateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldStateTheme {
                SquadApp()
            }
        }
    }
}
