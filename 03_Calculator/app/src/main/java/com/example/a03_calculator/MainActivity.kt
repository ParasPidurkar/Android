package com.example.a03_calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val number1 = findViewById<EditText>(R.id.edt1)
        val number2 = findViewById<EditText>(R.id.edt2)
        val addBtn = findViewById<Button>(R.id.add_btn)
        val subBtn = findViewById<Button>(R.id.sub_btn)
        val mulBtn = findViewById<Button>(R.id.mul_btn)
        val divBtn = findViewById<Button>(R.id.div_btn)
        val res = findViewById<TextView>(R.id.txt2)

        // Function to validate inputs
        fun areInputsValid(): Boolean {
            val num1Text = number1.text.toString()
            val num2Text = number2.text.toString()
            return when {
                num1Text.isEmpty() -> {
                    res.text = "Please enter number 1"
                    false
                }
                num2Text.isEmpty() -> {
                    res.text = "Please enter number 2"
                    false
                }
                else -> true
            }
        }

        // Add button functionality
        addBtn.setOnClickListener {
            if (areInputsValid()) {
                val num1 = number1.text.toString().toDouble()
                val num2 = number2.text.toString().toDouble()
                val result = num1 + num2
                res.text = "Addition Result: $result"
            }
        }

        // Subtract button functionality
        subBtn.setOnClickListener {
            if (areInputsValid()) {
                val num1 = number1.text.toString().toDouble()
                val num2 = number2.text.toString().toDouble()
                val result = num1 - num2
                res.text = "Subtraction Result: $result"
            }
        }

        // Multiply button functionality
        mulBtn.setOnClickListener {
            if (areInputsValid()) {
                val num1 = number1.text.toString().toDouble()
                val num2 = number2.text.toString().toDouble()
                val result = num1 * num2
                res.text = "Multiplication Result: $result"
            }
        }

        // Divide button functionality
        divBtn.setOnClickListener {
            if (areInputsValid()) {
                val num1 = number1.text.toString().toDouble()
                val num2 = number2.text.toString().toDouble()
                if (num2 != 0.0) {
                    val result = num1 / num2
                    res.text = "Division Result: $result"
                } else {
                    res.text = "Cannot divide by zero"
                }
            }
        }
    }
}
