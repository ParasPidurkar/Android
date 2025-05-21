package com.example.listviewvalo

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val users = arrayOf("Brimstone", "Viper", "Killjoy", "Sage", "Waylay")

        val listView = findViewById<ListView>(R.id.userlist)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
        listView.adapter = arrayAdapter

        // Set item click listener
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            Toast.makeText(this, "Clicked: $selectedItem", Toast.LENGTH_SHORT).show()
        }

    }
}