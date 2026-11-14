package com.paras.navigation

data class Agent(
    val id: String,
    val name: String,
    val role: String,
    val origin: String,
    val ultimate: String,
)

object ValoData {
    val agents = listOf(
        Agent("jett", "Jett", "Duelist", "South Korea", "Blade Storm"),
        Agent("phoenix", "Phoenix", "Duelist", "United Kingdom", "Run It Back"),
        Agent("sova", "Sova", "Initiator", "Russia", "Hunter's Fury"),
        Agent("skye", "Skye", "Initiator", "Australia", "Seekers"),
        Agent("brimstone", "Brimstone", "Controller", "USA", "Orbital Strike"),
        Agent("omen", "Omen", "Controller", "Unknown", "From the Shadows"),
        Agent("sage", "Sage", "Sentinel", "China", "Resurrection"),
        Agent("killjoy", "Killjoy", "Sentinel", "Germany", "Lockdown"),
    )

    val maps = listOf("Ascent", "Bind", "Haven", "Split", "Icebox", "Breeze", "Lotus", "Sunset")

    fun agent(id: String): Agent = agents.first { it.id == id }
}
