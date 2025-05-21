package com.example.passing_idata_to_ntent

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecondActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_second)

            val textname: TextView = findViewById(R.id.textView)
            val firstName = intent.getStringExtra("extra_first_name")
            val lastName = intent.getStringExtra("extra_last_name")


            textname.text = "My name is $firstName  $lastName"
        }
    }