package com.example.validationapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.loginvalidation.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnValidate = findViewById<Button>(R.id.btnValidate)


        val collegeDomain = "rajalakshmi.edu"

        btnValidate.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()


            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ii) Email must be proper + college domain
            val isValidEmailFormat = Patterns.EMAIL_ADDRESS.matcher(email).matches()
            val isCollegeEmail = email.endsWith("@$collegeDomain", ignoreCase = true)

            if (!isValidEmailFormat || !isCollegeEmail) {
                Toast.makeText(this, "Enter a valid college email (ends with @$collegeDomain)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            val passwordRegex = Regex(
                "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!_\\-]).{12,}$"
            )

            if (!passwordRegex.matches(password)) {
                Toast.makeText(
                    this,
                    "Password must be 12+ chars with 1 uppercase, 1 number, 1 special symbol",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Validation Successful ✅", Toast.LENGTH_LONG).show()
        }
    }
}