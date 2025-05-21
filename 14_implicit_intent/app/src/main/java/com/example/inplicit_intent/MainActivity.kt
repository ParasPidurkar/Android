package com.example.inplicit_intent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val openUrlButton: Button = findViewById(R.id.buttonOpenUrl)
        val EdtText : EditText = findViewById(R.id.edtText)

        // Set a click listener to open the URL when clicked
        openUrlButton.setOnClickListener {

            val name: String = EdtText.getText().toString()
            // Create an implicit intent to open the web URL
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(name)
                //https://paraspidurkar.github.io/player/
            }

            // Start the activity, which will open the browser
            startActivity(intent)
        }
    }
}