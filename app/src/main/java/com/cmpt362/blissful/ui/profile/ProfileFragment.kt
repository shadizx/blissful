package com.cmpt362.blissful.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.MainActivity
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentProfileBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.user.User
import com.cmpt362.blissful.db.user.UserDatabaseDao
import com.cmpt362.blissful.db.user.UserRepository
import com.cmpt362.blissful.db.user.UserViewModel
import com.cmpt362.blissful.db.user.UserViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import com.cmpt362.blissful.db.util.signOut
import com.cmpt362.blissful.ui.home.GratitudeAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var database: LocalRoomDatabase
    private lateinit var userDatabaseDao: UserDatabaseDao
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModelFactory: UserViewModelFactory
    private lateinit var userViewModel: UserViewModel

    private lateinit var postsDatabaseDao: PostDatabaseDao
    private lateinit var postsRepository: PostRepository
    private lateinit var postsViewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    // user posts
    private lateinit var userPostsArrayList: ArrayList<Post>
    private lateinit var userPostsAdapter: GratitudeAdapter
    private lateinit var userPostsRecyclerView: RecyclerView

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

    private companion object LoginActivity {
        private const val TAG = "LoginActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleActivityLauncher: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewFlipper = root.findViewById(R.id.view_flipper)
        getCredentials()
        setUpPage()
        setUpGoogle()

        requireActivity().getSharedPreferences("user", 0)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        settingsButton = root.findViewById(R.id.buttonSetting)
        settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun setUpGoogle() {
        setupDatabase()
        setGoogleLogin()
        googleActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }
    }

    private fun setGoogleLogin() {
        // Initialize Firebase Auth
        auth = Firebase.auth

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
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
            val username = userRepository.getUsernameForUserId(userId)
            val data = "Hello, $username!"
            profileText.text = data
        }

        initializeAdapter()
        updateDisplayedPosts()
    }

    private fun updateDisplayedPosts() {
        lifecycleScope.launch {
            postViewModel.getPostsByUserId(userId).observe(viewLifecycleOwner) {
                userPostsAdapter.setData(it)
                userPostsAdapter.notifyDataSetChanged()
                userPostsRecyclerView.adapter = userPostsAdapter
            }
        }
    }

    private fun initializeAdapter() {
        userPostsRecyclerView = viewFlipper.findViewById(R.id.user_posts)
        userPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userPostsArrayList = ArrayList()
        userPostsAdapter = GratitudeAdapter(userPostsArrayList)
        userPostsRecyclerView.adapter = userPostsAdapter
        userPostsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun setUpSignedOutPage() {
        val signInButton: Button = viewFlipper.findViewById(R.id.sign_in_button)
        val signUpButton: Button = viewFlipper.findViewById(R.id.sign_up_button)
        val googleSignInButton: SignInButton = viewFlipper.findViewById(R.id.btnSignIn)

        signInButton.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(activity, SignUpActivity::class.java)
            startActivity(intent)
        }

        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleActivityLauncher.launch(signInIntent)
        }
    }

    private fun setupDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())

        userDatabaseDao = database.userDatabaseDao
        userRepository = UserRepository(userDatabaseDao)
        userViewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]

        postsDatabaseDao = database.postDatabaseDao
        postsRepository = PostRepository(postsDatabaseDao)
        postsViewModelFactory = PostViewModelFactory(postsRepository)
        postViewModel = ViewModelProvider(this, postsViewModelFactory)[PostViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().getSharedPreferences("user", 0)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        _binding = null
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    signUpUser(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                    Log.w(TAG, "user not signed in..")
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth.currentUser
    }


    private fun signUpUser(user: FirebaseUser?) {
        userViewModel.checkUserForLogin(user?.displayName.toString(), "")
            .observe(viewLifecycleOwner) { exists ->
                // Check if the user exists, if yes send toast, if no create a new user
                if (exists) {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT)
                        .show()
                    onSignedIn(user?.displayName.toString())
                } else {
                    val newUser = User(username = user?.displayName.toString(), password = "")
                    userViewModel.insert(newUser).observe(viewLifecycleOwner) { id ->
                        if (id != null) {
                            val sharedPreferences =
                                requireActivity().getSharedPreferences("user", 0)
                            val editor = sharedPreferences.edit()
                            editor.putInt("userId", id)
                            editor.apply()

                        }
                    }
                }
            }
    }

    // Send flag that google login is successful to profile fragment
    private fun onSignedIn(username: String) {
        userViewModel.getIdForUser(username).observe(viewLifecycleOwner) { id ->
            val sharedPreferences = requireActivity().getSharedPreferences("user", 0)
            val editor = sharedPreferences.edit()
            editor.putInt("userId", id)
            editor.apply()
        }
    }
}
