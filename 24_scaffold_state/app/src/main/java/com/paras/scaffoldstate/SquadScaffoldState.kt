package com.paras.scaffoldstate

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Material 2 had one object for this: `rememberScaffoldState()`, which held the
 * drawer state and the snackbar state together. Material 3 removed it. Now each
 * component has its own state object, and you create the ones you need.
 *
 * This class groups them again, so the rest of the app has one thing to pass around.
 * It's an ordinary Kotlin class, not a Compose API. You could skip it and keep three
 * separate variables.
 *
 *   drawerState        -> is the drawer open or closed? (ModalNavigationDrawer)
 *   snackbarHostState  -> which snackbar is showing, and which are queued (SnackbarHost)
 *   topBarState        -> how far the top bar has scrolled out of view (TopAppBar)
 *   scope              -> open(), close() and showSnackbar() are suspend functions,
 *                         so click handlers need a coroutine scope to call them
 */
@OptIn(ExperimentalMaterial3Api::class)
@Stable
class SquadScaffoldState(
    val drawerState: DrawerState,
    val snackbarHostState: SnackbarHostState,
    val topBarState: TopAppBarState,
    val scope: CoroutineScope,
) {
    // Animates the drawer open. drawerState.open() suspends until the animation ends.
    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    fun toggleDrawer() {
        if (drawerState.isClosed) openDrawer() else closeDrawer()
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }
}

/**
 * The "remember" function for the holder, like Material 2's rememberScaffoldState().
 *
 * Which parts survive rotation:
 *   rememberDrawerState     -> uses rememberSaveable inside, so it survives
 *   rememberTopAppBarState  -> uses rememberSaveable inside, so it survives
 *   SnackbarHostState       -> no Saver exists, so a showing snackbar is lost
 *
 * Pass `initialDrawerValue = DrawerValue.Open` to start with the drawer open.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSquadScaffoldState(
    initialDrawerValue: DrawerValue = DrawerValue.Closed,
): SquadScaffoldState {
    val drawerState = rememberDrawerState(initialDrawerValue)
    val snackbarHostState = remember { SnackbarHostState() }
    val topBarState = rememberTopAppBarState()
    val scope = rememberCoroutineScope()
    return remember(drawerState, snackbarHostState, topBarState, scope) {
        SquadScaffoldState(drawerState, snackbarHostState, topBarState, scope)
    }
}
