package com.example.userpreferences

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etAge: EditText
    private lateinit var etBio: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)


        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAge = findViewById(R.id.etAge)
        etBio = findViewById(R.id.etBio)
        radioGroup = findViewById(R.id.radioGroupColor)
        btnSave = findViewById(R.id.btnSave)

        loadData()

        btnSave.setOnClickListener {


            if (etName.text.isEmpty() || etEmail.text.isEmpty()) {
                Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sharedPreferences.edit {

                putString("name", etName.text.toString())
                putString("email", etEmail.text.toString())
                putString("bio", etBio.text.toString())


                val age = etAge.text.toString()
                    .toIntOrNull() ?: 0
                putInt("age", age)


                val selectedColorId = radioGroup.checkedRadioButtonId
                if (selectedColorId != -1) {
                    putInt("color", selectedColorId)
                }

            }

            Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        etName.setText(sharedPreferences.getString("name", ""))
        etEmail.setText(sharedPreferences.getString("email", ""))
        etBio.setText(sharedPreferences.getString("bio", ""))


        val age = sharedPreferences.getInt("age", 0)
        if (age != 0) {
            etAge.setText(age.toString())
        }


        val savedColorId = sharedPreferences.getInt("color", -1)
        if (savedColorId != -1) {
            radioGroup.check(savedColorId)
        }

        Toast.makeText(this, "Profile Loaded", Toast.LENGTH_SHORT).show()
    }
}