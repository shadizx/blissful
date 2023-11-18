package com.cmpt362.blissful.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmpt362.blissful.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        // Here, add your logic to verify the credentials
        if (username.isNotEmpty() && password.isNotEmpty()) {
            Toast.makeText(this, "Register Successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show()
        }
    }
}
