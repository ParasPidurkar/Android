package com.paras.navigation

data class Agent(
    val id: String,
    val name: String,
    val role: String,
    val origin: String,
    val ultimate: String,
)

/**
 * The app's "database": a simple list in code.
 *
 * Because it's in the code, the data is always there, even after Android kills and restarts
 * the app. That's why passing only an ID is safe: ValoData.agent("jett") always finds Jett.
 *
 * In a real app this would be a repository (Room database or a server), and the screen
 * would load the data by ID, usually inside a ViewModel.
 */
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

        /**
     * Find an agent by the ID that came from navigation.
     *
     * first { } crashes if the ID doesn't exist. That's OK here, because IDs only come from
     * our own list. If anyone could type the ID (like in a link), you would use firstOrNull
     * and show an error instead. 27_deep_links shows that.
     */
    fun agent(id: String): Agent = agents.first { it.id == id }
}
