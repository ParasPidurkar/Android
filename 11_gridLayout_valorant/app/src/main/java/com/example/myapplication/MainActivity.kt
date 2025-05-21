package com.example.myapplication

import AgentAdapter
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var agentAdapter: AgentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewAgents)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        val agents = getValorantAgents()
        agentAdapter = AgentAdapter(agents)
        recyclerView.adapter = agentAdapter
    }

    // Build a list of Valorant agents. Replace the image resource IDs with your own.
    private fun getValorantAgents(): List<Agent> {
        return listOf(
            Agent("Brimstone", R.drawable.agent_brimstone),
            Agent("Chamber", R.drawable.agent_chamber),
            Agent("Sage", R.drawable.agent_sage),
            Agent("Sova", R.drawable.agent_sova),
            Agent("Viper", R.drawable.agent_viper),
            Agent("Cypher", R.drawable.agent_cypher),
            Agent("Reyna", R.drawable.agent_reyna),
            Agent("Jett", R.drawable.agent_jett),
            Agent("Raze", R.drawable.agent_raze),
            Agent("Omen", R.drawable.agent_omen),
            Agent("KAY/O", R.drawable.agent_kayo)
            // Add more agents as needed
        )
    }
}