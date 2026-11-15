package com.paras.deeplinks

import kotlinx.serialization.Serializable

/**
 * ROUTES: one Kotlin type for each screen.
 *
 * A route answers two questions: "which screen?" and "with what data?".
 *
 *   - Screen with NO data   -> an object       (e.g. Lobby)
 *   - Screen WITH data      -> a data class    (e.g. AgentSelect(map))
 *     The properties of the class ARE the data the screen needs.
 *
 * WHY @Serializable?
 * Inside, Navigation still uses text paths (like a website URL) and saves data in a Bundle.
 * @Serializable lets Navigation do the conversion for us:
 *
 *   navigate(AgentDetail("jett", "Ascent"))
 *        -> becomes the text   ".../AgentDetail/jett/Ascent"
 *   entry.toRoute<AgentDetail>()
 *        -> turns it back into AgentDetail(agentId = "jett", map = "Ascent")
 *
 * Properties WITHOUT a default value  -> part of the path:   /jett/Ascent
 * Properties WITH a default (or null) -> optional extras:    ?skin=gold
 *
 * THE OLD WAY (text routes), which you'll still see in older tutorials:
 *
 *     composable("agentDetail/{agentId}/{map}") { ... }
 *     navController.navigate("agentDetail/jett/Ascent")
 *
 * A typo in those strings compiles fine and crashes when the app runs.
 * With types, the compiler finds the mistake for you.
 *
 * WHAT CAN BE SENT AS DATA?
 * Simple types: String, Int, Long, Float, Double, Boolean (and nullable versions).
 * For anything bigger, send an ID and look the object up on the other screen.
 * That's what this app does with agentId.
 */

/** First screen. No data, so it's an object. */
@Serializable
object Lobby

/** Map list. Gets no data, but SENDS a result back (see PICKED_MAP_KEY). */
@Serializable
object MapPick

/** Agent list. Needs to know the chosen map. */
@Serializable
data class AgentSelect(val map: String)

/**
 * One agent. Takes the agent's ID, not the Agent object.
 * The data is saved in a Bundle, which should stay small and simple.
 */
@Serializable
data class AgentDetail(val agentId: String, val map: String)

/** Match result. Opened with popUpTo<Lobby>, so the back stack is [Lobby, Match]. */
@Serializable
data class Match(val agentId: String, val map: String)

/** Opened from the 👤 icon with launchSingleTop. */
@Serializable
object Career

/**
 * The key (name) used to send the picked map from MapPick back to Lobby.
 * Both screens use this one constant, so they can't spell it differently by mistake.
 */
const val PICKED_MAP_KEY = "picked_map"

/**
 * The start of every deep link (a link that opens a screen of this app directly).
 *
 * It must match <data android:scheme="valo" android:host="app"/> in AndroidManifest.xml.
 *   - The manifest tells Android: "links starting with valo://app belong to this app".
 *   - Each navDeepLink in ValoNavApp then says which SCREEN opens for which link.
 *
 *   Link                                  Opens                        Back stack
 *   valo://app/lobby                      Lobby                        [Lobby]
 *   valo://app/career                     Career                       [Lobby, Career]
 *   valo://app/agents/{map}               AgentSelect(map)             [Lobby, AgentSelect]
 *   valo://app/agent/{agentId}/{map}      AgentDetail(agentId, map)    [Lobby, AgentDetail]
 *
 * The {parts} come from the route's properties. AgentDetail has agentId and map,
 * so its link ends with /{agentId}/{map}.
 *
 * Careful: if you rename a property, the link changes too, and links people already shared
 * stop working.
 *
 * NOT linkable, on purpose:
 *   - MapPick: it only works when the Lobby is below it, waiting for the result.
 *   - Match:   you should only get there by pressing LOCK IN, not by typing a link.
 */
const val DEEP_LINK_BASE = "valo://app"
