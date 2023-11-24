package com.cmpt362.blissful.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory

class SignUpActivity : AppCompatActivity() {

    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: UserDatabaseDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    private lateinit var username: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = LocalRoomDatabase.getInstance(this)
        databaseDao = database.userDatabaseDao
        repository = UserRepository(databaseDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            validateInput()
        }
    }

    private fun validateInput() {
        username = editTextUsername.text.toString()
        password = editTextPassword.text.toString()

        if (username.isNotEmpty() && password.isNotEmpty()) {
            checkIfUsernameTaken()
        } else {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun checkIfUsernameTaken() {
        userViewModel.checkIsUsernameTaken(username).observe(this) { isUsernameTaken ->
            if (isUsernameTaken != null) {
                if (isUsernameTaken) {
                    Toast.makeText(this, "Username is already taken", Toast.LENGTH_SHORT).show()
                } else {
                    registerUser()
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Username check in progress...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser() {
        val user = User(username = username, password = password)
        userViewModel.insert(user).observe(this) { id ->
            if (id != null) {
                val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("userId", id)
                editor.apply()
                finish()
            }
        }

    }
}
