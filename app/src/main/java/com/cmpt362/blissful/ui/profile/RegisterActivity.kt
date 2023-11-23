package com.cmpt362.blissful.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.R
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory

class RegisterActivity : AppCompatActivity() {

    // DB instances
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

        // Initialize DB instances
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
            registerUser()
            Toast.makeText(this, "Register Successful", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun registerUser() {
        // Create new user record
        val newUserRecord = User(0, username, password)

        // Insert the new user by the view model, into the Room DB
        userViewModel.insert(newUserRecord)
    }
}
