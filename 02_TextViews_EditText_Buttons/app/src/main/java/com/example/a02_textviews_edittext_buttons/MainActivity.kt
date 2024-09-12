package com.example.a02_textviews_edittext_buttons

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to UI elements
        val agentNameTextView = findViewById<TextView>(R.id.agentNameTextView)
        val agentRoleTextView = findViewById<TextView>(R.id.agentRoleTextView)
       // val agentImageView = findViewById<ImageView>(R.id.//agentImageView)
        val editTextView = findViewById<EditText>(R.id.editTextView)
        val abilitiesButton = findViewById<Button>(R.id.abilitiesButton)

        // Set static data about Jett
        agentNameTextView.text = "Jett"
        agentRoleTextView.text = "Duelist"
       // agentImageView.setImageResource(R.drawable.jett_image)  // Add an image for Jett in res/drawable

        // Set up button click event
        abilitiesButton.setOnClickListener {
            val userInput = editTextView.text.toString()
            if (userInput.isNotEmpty()) {
                Toast.makeText(this, "Jett's abilities: Tailwind, Updraft, Cloudburst, Blade Storm", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please enter some input.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}