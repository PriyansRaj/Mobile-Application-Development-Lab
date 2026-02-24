package com.example.techlounge

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val etMarks = findViewById<EditText>(R.id.etMarks)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        btnCalculate.setOnClickListener {
            val name = etName.text.toString()
            val mark = etMarks.text.toString()
            if (name.isEmpty() || mark.isEmpty()){
                tvResult.text = "Please fill all fields"
                return@setOnClickListener
            }

            val marks = mark.toInt()
            if (marks !in 0..100) {
                etMarks.error = "Marks must be 0-100"
                return@setOnClickListener
            }
            val grade = when{
                marks >=90-> "A+"
                marks >=80->"A"
                marks >=70 ->"B+"
                marks >=60 ->"B"
                marks >=50 ->"C"
                else -> "Fail"

            }
            val result = if(marks >=50) "Pass" else "Fail"
            tvResult.text = """
                Name: $name
                Marks: $marks
                Result: $result
                Grade: $grade
            """.trimIndent()

        }

    }
}