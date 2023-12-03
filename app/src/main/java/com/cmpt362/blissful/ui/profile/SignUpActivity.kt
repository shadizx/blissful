package com.cmpt362.blissful.ui.profile

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var signUpButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initializeFirebase()
        setUpViews()
    }

    private fun initializeFirebase() {
        val userRepository = UserRepository(FirebaseFirestore.getInstance())
        val viewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]
    }

    private fun setUpViews() {
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonConfirm)
        cancelButton = findViewById(R.id.buttonCancel)

        signUpButton.text = getString(R.string.sign_up)
        signUpButton.setOnClickListener { validateInput() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun validateInput() {
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            checkIfUsernameTaken(username, password)
        } else {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkIfUsernameTaken(username: String, password: String) {
        userViewModel.checkIsUsernameTaken(username).observe(this) { isUsernameTaken ->
            if (isUsernameTaken) {
                Toast.makeText(this, "Username is already taken", Toast.LENGTH_SHORT).show()
            } else {
                signUpUser(username, password)
            }
        }
    }

    private fun signUpUser(username: String, password: String) {
        val user = User(username = username, password = password)
        userViewModel.insert(user).observe(this) { userId ->
            if (userId != null) {
                // Handle successful registration
                val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userId", userId)
                editor.apply()

                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}