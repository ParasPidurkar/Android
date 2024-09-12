package com.example.a01_layouts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the default layout to Linear Layout
        setContentView(R.layout.linear_layout)

        // You can switch layouts by replacing the content view.
        // Uncomment the one you want to use to check:

        // setContentView(R.layout.constraint_layout)
        // setContentView(R.layout.relative_layout)
    }
}