package com.cmpt362.blissful.ui.profile

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var signInButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeFirebase()
        setUpViews()
    }

    private fun initializeFirebase() {
        val userRepository = UserRepository(FirebaseFirestore.getInstance())
        val viewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)
    }

    private fun setUpViews() {
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        signInButton = findViewById(R.id.buttonConfirm)
        cancelButton = findViewById(R.id.buttonCancel)

        signInButton.text = getString(R.string.sign_in)
        signInButton.setOnClickListener { signInUser() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun signInUser() {
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            // Here you may want to use Firebase Authentication instead
            userViewModel.checkUserForLogin(username, password).observe(this) { exists ->
                if (exists) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    onSignedIn(username)
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onSignedIn(username: String) {
        userViewModel.getIdForUser(username).observe(this) { userId ->
            val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("userId", userId)
            editor.putString("userName", username)
            editor.apply()
        }
        setResult(Activity.RESULT_OK)
        finish()
    }
}
