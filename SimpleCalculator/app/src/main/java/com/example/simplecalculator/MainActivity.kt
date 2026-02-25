package com.example.simplecalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val num1 = findViewById<EditText>(R.id.etNumber1)
        val num2 = findViewById<EditText>(R.id.etNumber2)
        val result = findViewById<TextView>(R.id.tvResult)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnSub = findViewById<Button>(R.id.btnSub)
        val btnMul = findViewById<Button>(R.id.btnMul)
        val btnDiv = findViewById<Button>(R.id.btnDiv)

        fun getNumbers(): Pair<Double, Double>? {
            val n1 = num1.text.toString()
            val n2 = num2.text.toString()

            if (n1.isEmpty() || n2.isEmpty()) {
                Toast.makeText(this, "Enter both numbers", Toast.LENGTH_SHORT).show()
                return null
            }

            return Pair(n1.toDouble(), n2.toDouble())
        }

        btnAdd.setOnClickListener {
            getNumbers()?.let {
                result.text = "Result: ${it.first + it.second}"
            }
        }

        btnSub.setOnClickListener {
            getNumbers()?.let {
                result.text = "Result: ${it.first - it.second}"
            }
        }

        btnMul.setOnClickListener {
            getNumbers()?.let {
                result.text = "Result: ${it.first * it.second}"
            }
        }

        btnDiv.setOnClickListener {
            getNumbers()?.let {
                if (it.second == 0.0) {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show()
                } else {
                    result.text = "Result: ${it.first / it.second}"
                }
            }
        }
    }
}