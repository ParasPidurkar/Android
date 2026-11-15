package com.paras.deeplinks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paras.deeplinks.ui.theme.ValoTheme

/**
 * The ONLY Activity in the app. This is called "single-Activity" style.
 *
 * Old way (with Intents):
 *   LobbyActivity, MapPickActivity, AgentSelectActivity, ... one Activity per screen,
 *   all listed in AndroidManifest.xml, opened with startActivity().
 *   Android kept the list of open screens.
 *
 * New way (this app):
 *   One Activity. Every screen is a composable function.
 *   The NavController in ValoNavApp() keeps the list of open screens.
 *
 * The phone's Back button still works: the NavHost catches it and closes the top screen.
 * When only Lobby is left, the NavHost lets the Activity handle Back, and the app closes.
 *
 * DEEP LINKS
 * A link like valo://app/career also starts this Activity (see the manifest).
 * There's no deep link code in this file. It all lives in ValoNavApp:
 *   - App was closed: the NavController reads the link from the Activity by itself.
 *   - App was open:   ValoNavApp listens for the new link and passes it to the NavController.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        // After a rotation, savedInstanceState holds the saved back stack.
        // You don't need to touch it: rememberNavController() restores it for you.
        super.onCreate(savedInstanceState)
        setContent {
            ValoTheme {
                ValoNavApp()
            }
        }
    }
}
