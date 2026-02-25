package com.example.validation



import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etID = findViewById<EditText>(R.id.etID)
        val btnValidate = findViewById<Button>(R.id.btnValidate)

        btnValidate.setOnClickListener {

            val username = etUsername.text.toString().trim()
            val id = etID.text.toString().trim()

            // i) Both fields should not be empty
            if (username.isEmpty() || id.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ii) Username should contain only alphabets
            val nameRegex = Regex("^[A-Za-z]+$")

            if (!nameRegex.matches(username)) {
                Toast.makeText(this, "Username should contain only alphabets", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // iii) ID should contain only 4-digit numeric value
            val idRegex = Regex("^[0-9]{4}$")

            if (!idRegex.matches(id)) {
                Toast.makeText(this, "ID must be exactly 4 digits", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Validation Successful ✅", Toast.LENGTH_LONG).show()
        }
    }
}