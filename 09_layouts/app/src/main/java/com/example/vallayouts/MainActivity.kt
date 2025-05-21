package com.example.vallayouts

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the Button and TextViews from the layout
        val triggerButton: Button = findViewById(R.id.triggerAbilityButton)
        val abilityText: TextView = findViewById(R.id.specialAbility)

        // Set a click listener on the button
        triggerButton.setOnClickListener {
            abilityText.text = "Ultimate: Open up the Sky Activated!"
        }
    }
}