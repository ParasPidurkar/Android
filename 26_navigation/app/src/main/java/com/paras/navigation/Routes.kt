package com.paras.navigation

import kotlinx.serialization.Serializable

/**
 * ============================================================================================
 *  ROUTES: one Kotlin type per screen (type-safe navigation, Navigation 2.8+)
 * ============================================================================================
 *
 * A ROUTE answers "which screen, and with what data?".
 *
 *  - A screen with no arguments  -> a @Serializable object
 *  - A screen that needs data    -> a @Serializable data class; its properties ARE the arguments
 *
 * WHY @Serializable?
 * Underneath, Navigation still works with URL-like strings and a Bundle of arguments. The
 * kotlinx.serialization compiler plugin generates a serializer for each class, and Navigation
 * uses it to:
 *   1. build the route PATTERN when you register composable<AgentDetail> { }:
 *          com.paras.navigation.AgentDetail/{agentId}/{map}
 *   2. fill in the values when you call navigate(AgentDetail("jett", "Ascent")):
 *          com.paras.navigation.AgentDetail/jett/Ascent
 *   3. decode the arguments Bundle back into an AgentDetail when you call toRoute<AgentDetail>().
 *
 * Required properties (no default value, not nullable) become PATH segments: /{agentId}.
 * Properties with a default value, or a nullable type, become QUERY parameters: ?map={map}.
 *
 * THE OLD WAY (string routes), for comparison. You'll still see this in older tutorials:
 *
 *     composable(
 *         "agentDetail/{agentId}/{map}",
 *         arguments = listOf(navArgument("agentId") { type = NavType.StringType }, ...)
 *     ) { entry -> val id = entry.arguments?.getString("agentId") }
 *
 *     navController.navigate("agentDetail/jett/Ascent")
 *
 * A typo in any of those strings compiles fine and crashes at runtime. With types, the compiler
 * catches a misspelled property, a missing argument or a wrong type.
 *
 * WHAT CAN BE AN ARGUMENT?
 * Out of the box: String, Int, Long, Float, Double, Boolean, their nullable versions, and some
 * lists/arrays of them. Anything else needs a custom NavType passed as a `typeMap`. That's
 * usually a sign you should pass an ID instead and look the object up on the other side.
 * That's what this app does with agentId.
 */

/** Start destination. No arguments, so it's an object. */
@Serializable
object Lobby

/** Map picker. No arguments in; it sends a RESULT back out (see PICKED_MAP_KEY). */
@Serializable
object MapPick

/** Agent list. Needs to know which map was chosen, so `map` is a required argument. */
@Serializable
data class AgentSelect(val map: String)

/**
 * One agent. Two arguments. Note it takes the agent's ID, not an Agent object: arguments are
 * stored in a Bundle that must survive process death.
 */
@Serializable
data class AgentDetail(val agentId: String, val map: String)

/** The match result screen. Reached with popUpTo<Lobby>, so the stack is [Lobby, Match]. */
@Serializable
data class Match(val agentId: String, val map: String)

/** Opened from the 👤 icon with launchSingleTop. */
@Serializable
object Career

/**
 * Key used to send the picked map from MapPick back to Lobby through the Lobby entry's
 * SavedStateHandle. Keeping it in one constant means the sender and receiver can't disagree
 * on the spelling. (Results are still untyped key/value pairs, unlike routes.)
 */
const val PICKED_MAP_KEY = "picked_map"

/**
 * Base URI for every deep link into this app. Must match the <data android:scheme/host> in
 * AndroidManifest.xml, otherwise Android never delivers the Intent to MainActivity.
 *
 *   valo://app/lobby
 *   valo://app/career
 *   valo://app/agents/{map}                 -> AgentSelect(map)
 *   valo://app/agent/{agentId}/{map}        -> AgentDetail(agentId, map)
 *
 * Deliberately NOT deep-linkable:
 *   - MapPick: it only makes sense when someone (Lobby) is waiting for its result.
 *   - Match:   you should only get there by locking in, not by typing a URL.
 */
const val DEEP_LINK_BASE = "valo://app"
