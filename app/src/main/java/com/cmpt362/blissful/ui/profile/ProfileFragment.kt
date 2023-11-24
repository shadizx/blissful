package com.cmpt362.blissful.ui.profile

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cmpt362.blissful.databinding.FragmentProfileBinding
import com.cmpt362.blissful.db.util.getUserId
import com.cmpt362.blissful.db.util.signOut

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var userId: Int = -1
    private var isSignedIn: Boolean = false
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            updateButtonsVisibility()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        getCredentials()
        updateButtonsVisibility()
        requireActivity().getSharedPreferences("user", 0)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        return root
    }

    private fun updateButtonsVisibility() {
        val profileText: String
        if (isSignedIn) {
            binding.signInButton.visibility = View.GONE
            binding.signUpButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE

            if (!binding.signOutButton.hasOnClickListeners()) {
                binding.signOutButton.setOnClickListener {
                    signOut(requireContext())
                }
            }

            profileText = "Signed in with id: $userId"
        } else {
            binding.signOutButton.visibility = View.GONE
            binding.signInButton.visibility = View.VISIBLE
            binding.signUpButton.visibility = View.VISIBLE

            if (!binding.signInButton.hasOnClickListeners()) {
                binding.signInButton.setOnClickListener {
                    startActivity(Intent(activity, SignInActivity::class.java))
                }
            }
            if (!binding.signUpButton.hasOnClickListeners()) {
                binding.signUpButton.setOnClickListener {
                    startActivity(Intent(activity, SignUpActivity::class.java))
                }
            }

            profileText = "Not signed in"
        }
        binding.profileText.text = profileText
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != -1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().getSharedPreferences("user", 0)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        _binding = null
    }
}
