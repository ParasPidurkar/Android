package com.example.brim_ult

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
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

        val triggerButton = findViewById<Button>(R.id.triggerAbilityButton)
        val ultactivetext = findViewById<TextView>(R.id.ultactive)
        // Simulate progress change (can be changed to real data)

        // Set up the button click listener
        triggerButton.setOnClickListener {
            // Handle triggering ability (for example)
            println("Ability Triggered!")
            ultactivetext.text ="Open up the sky"

            val mediaPlayer = MediaPlayer.create(this, R.raw.brim_ult)
            mediaPlayer.start()

            Handler().postDelayed({
                ultactivetext.text = "" // or any other default text you want
            }, 2000)
        }
    }
}