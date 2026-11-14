package com.paras.navigation

import kotlinx.serialization.Serializable

/**
 * Every screen in the app is a ROUTE.
 *
 * - A screen with no arguments  -> a @Serializable object
 * - A screen that needs data    -> a @Serializable data class; its properties ARE the arguments
 *
 * Navigation turns these into a URL-like string for you (e.g. ".../AgentDetail/jett/Ascent"),
 * so you never write route strings by hand and the compiler checks every argument.
 */

@Serializable
object Lobby

@Serializable
object MapPick

@Serializable
data class AgentSelect(val map: String)

@Serializable
data class AgentDetail(val agentId: String, val map: String)

@Serializable
data class Match(val agentId: String, val map: String)

@Serializable
object Career

/** Key used to send the picked map from MapPick back to Lobby through SavedStateHandle. */
const val PICKED_MAP_KEY = "picked_map"
