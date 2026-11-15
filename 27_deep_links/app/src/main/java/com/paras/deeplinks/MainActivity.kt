package com.paras.deeplinks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paras.deeplinks.ui.theme.ValoTheme

/**
 * The ONLY Activity in the app ("single-Activity architecture").
 *
 * With Intents you would have LobbyActivity, MapPickActivity, AgentSelectActivity, ... each
 * declared in AndroidManifest.xml, and startActivity() to move between them. The Android system
 * would own the back stack.
 *
 * Here there is one Activity. Every "screen" is a composable, and the back stack is owned by
 * the NavController inside ValoNavApp(). The system Back button still works: NavHost registers
 * a BackHandler that pops the NavController's stack. When that stack has only the start
 * destination left, NavHost stops handling Back, the Activity gets it, and the app closes.
 *
 * Intents are still used where they belong:
 *   - the LAUNCHER intent-filter in the manifest is how the home screen starts this Activity.
 *   - the VIEW intent-filter is how DEEP LINKS (valo://app/...) start it.
 *
 * DEEP LINKS AND THIS ACTIVITY
 * There's no deep-link code in this class, on purpose:
 *   - Cold start: the link Intent becomes `intent` (this.intent). The NavController created in
 *     ValoNavApp reads it by itself when the NavHost sets up the graph.
 *   - Warm start: launchMode="singleTop" makes Android call onNewIntent() on this instance.
 *     ValoNavApp registers an OnNewIntent listener on this Activity (ComponentActivity supports
 *     addOnNewIntentListener) and forwards the Intent to navController.handleDeepLink().
 * Keeping it in ValoNavApp means every navigation rule, deep links included, is in one file.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        // savedInstanceState is non-null after rotation or process death. You don't need to
        // touch it: rememberNavController() restores the whole back stack from it automatically.
        super.onCreate(savedInstanceState)
        setContent {
            ValoTheme {
                ValoNavApp()
            }
        }
    }
}
