import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Agent
import com.example.myapplication.R

class AgentAdapter(private val agents: List<Agent>) : RecyclerView.Adapter<AgentAdapter.AgentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item_agent, parent, false)
        return AgentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AgentViewHolder, position: Int) {
        val agent = agents[position]
        holder.textViewName.text = agent.name
        holder.imageViewAgent.setImageResource(agent.imageResId)
    }

    override fun getItemCount() = agents.size

    inner class AgentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewAgent: ImageView = itemView.findViewById(R.id.imageViewAgent)
        val textViewName: TextView = itemView.findViewById(R.id.textViewAgentName)
    }
}
