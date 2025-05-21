package com.example.actmove

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for MainActivity from activity_main.xml
        setContentView(R.layout.activity_main)

        // Find the button by its ID
        val buttonNavigate = findViewById<Button>(R.id.buttonNavigate)

        // Set a click listener on the button to start SecondActivity
        buttonNavigate.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }
}

