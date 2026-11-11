package com.paras.scaffold

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paras.scaffold.ui.theme.ScaffoldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the status bar and navigation bar. Scaffold then adds
        // their size to innerPadding, so content still lands in a safe area.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldTheme {
                AgentScaffold()
            }
        }
    }
}
