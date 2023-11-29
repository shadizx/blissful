package com.cmpt362.blissful.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentProfileBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import com.cmpt362.blissful.db.util.signOut
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: UserDatabaseDao
    private lateinit var repository: UserRepository
    private lateinit var viewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var settingsButton: ImageButton

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var userId: Int = -1
    private var isSignedIn: Boolean = false
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            setUpPage()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewFlipper = root.findViewById(R.id.view_flipper)
        getCredentials()
        setUpPage()

        requireActivity().getSharedPreferences("user", 0)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        settingsButton = root.findViewById(R.id.buttonSetting)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != -1
    }

    private fun setUpPage() {
        if (isSignedIn) {
            viewFlipper.displayedChild = 0 // Index of the signed-in view
            setUpSignedInPage()
        } else {
            viewFlipper.displayedChild = 1 // Index of the not signed-in view
            setUpSignedOutPage()
        }
    }

    private fun setUpSignedInPage() {
        setupDatabase()
        val signOutButton: Button = viewFlipper.findViewById(R.id.sign_out_button)
        val profileText: TextView = viewFlipper.findViewById(R.id.profile_text)

        signOutButton.setOnClickListener {
            signOut(requireContext())
            viewFlipper.displayedChild = 1 // Index of the not signed-in view
            setUpSignedOutPage()
        }

        lifecycleScope.launch {
            val username = repository.getUsernameForUserId(userId)
            profileText.text = "Hello, $username!"
        }
    }

    private fun setUpSignedOutPage() {
        val signInButton: Button = viewFlipper.findViewById(R.id.sign_in_button)
        val signUpButton: Button = viewFlipper.findViewById(R.id.sign_up_button)

        signInButton.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.userDatabaseDao
        repository = UserRepository(databaseDao)
        viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().getSharedPreferences("user", 0)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        _binding = null
    }
}
