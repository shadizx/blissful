package com.cmpt362.blissful.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory

class SignInActivity : AppCompatActivity() {

    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: UserDatabaseDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = LocalRoomDatabase.getInstance(this)
        databaseDao = database.userDatabaseDao
        repository = UserRepository(databaseDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonConfirm)
        buttonCancel = findViewById(R.id.buttonCancel)

        buttonLogin.text = getString(R.string.login)
        buttonLogin.setOnClickListener {
            loginUser()
        }
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun loginUser() {
        val username = editTextUsername.text.toString()
        val password = editTextPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            userViewModel.checkUserForLogin(username, password).observe(this) { exists ->
                if (exists) {
                    Toast.makeText(this@SignInActivity, "Login Successful", Toast.LENGTH_SHORT)
                        .show()
                    onLoginSucceed(username)
                } else {
                    Toast.makeText(
                        this@SignInActivity,
                        "Invalid username or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onLoginSucceed(username: String) {
        userViewModel.getIdForUser(username).observe(this) { id ->
            val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("userId", id)
            editor.apply()
        }
        finish()
    }
}
