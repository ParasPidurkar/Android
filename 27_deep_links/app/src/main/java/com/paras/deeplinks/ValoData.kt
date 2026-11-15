package com.paras.deeplinks

data class Agent(
    val id: String,
    val name: String,
    val role: String,
    val origin: String,
    val ultimate: String,
)

/**
 * The app's "database". It's a plain object, so the data exists in every process start
 * without being saved anywhere. That's what makes passing only an ID safe: after process death,
 * ValoData.agent("jett") still finds Jett. In a real app this would be a repository backed by
 * Room or a network API, and the screen would load by id (usually inside a ViewModel).
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
     * Look an agent up by the id that came through a navigation argument.
     * first { } throws if the id is unknown. That's fine for ids that came from this list.
     */
    fun agent(id: String): Agent = agents.first { it.id == id }

    /**
     * Deep-link-safe lookup. A deep link is a URL anyone can type (valo://app/agent/reyna/x),
     * so its arguments are UNTRUSTED input: return null instead of crashing on an unknown id.
     */
    fun agentOrNull(id: String): Agent? = agents.firstOrNull { it.id == id }
}
