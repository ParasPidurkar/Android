package com.example.passing_idata_to_ntent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
        val edtFirstName :EditText = findViewById(R.id.edt_first_name)
        val edtLastName : EditText = findViewById(R.id.edt_last_name)

        // Set a click listener on the button to start SecondActivity
        buttonNavigate.setOnClickListener {

            val edtFirstName = edtFirstName.text.toString()
            val edtLastName = edtLastName.text.toString()
            val intent123 = Intent(this, SecondActivity::class.java)
            intent123.putExtra("extra_first_name",edtFirstName)
            intent123.putExtra("extra_last_name",edtLastName)
            startActivity(intent123)
        }
    }
}