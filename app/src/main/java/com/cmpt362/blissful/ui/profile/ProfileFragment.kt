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
import android.widget.ProgressBar
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
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentProfileBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.user.User
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var postViewModel: PostViewModel

    private lateinit var database: LocalRoomDatabase
    private lateinit var postsDatabaseDao: PostDatabaseDao

    private lateinit var userPostsArrayList: ArrayList<Post>
    private lateinit var userPostsAdapter: GratitudeAdapter
    private lateinit var userPostsRecyclerView: RecyclerView

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var settingsButton: ImageButton

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var userId: String = ""
    private var isSignedIn: Boolean = false

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Database Variables
        initializeFirebaseAuth()
        initializeUserViewModel()
        initializeRoomDatabase()

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

    private fun initializeFirebaseAuth() {
        auth = Firebase.auth
    }

    private fun initializeUserViewModel() {
        val userRepository = UserRepository(FirebaseFirestore.getInstance())
        val userViewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelFactory)[UserViewModel::class.java]
    }

    private fun initializeRoomDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())
        postsDatabaseDao = database.postDatabaseDao
        val postRepository = PostRepository(postsDatabaseDao)
        val postsViewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, postsViewModelFactory)[PostViewModel::class.java]
    }

    private fun setUpGoogle() {
        setGoogleLogin()
        googleActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }
    }

    private fun setGoogleLogin() {
        // Initialize Firebase Auth
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != ""
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
        val signOutButton: Button = viewFlipper.findViewById(R.id.sign_out_button)
        val profileText: TextView = viewFlipper.findViewById(R.id.profile_text)

        signOutButton.setOnClickListener {
            signOut(requireContext())
            viewFlipper.displayedChild = 1 // Index of the not signed-in view
            setUpSignedOutPage()
        }

        lifecycleScope.launch {
            userViewModel.getUsernameForUserId(userId.toString())
                .observe(viewLifecycleOwner) { username ->
                    val data = "Hello, $username!"
                    profileText.text = data
                }
        }

        // Update achievements
        lifecycleScope.launch {
            postViewModel.getPostsByUserId(userId).observe(viewLifecycleOwner) { posts ->
                val numPosts = posts.size
                updateAchievementComponent(numPosts)
            }
        }

        // Update profile "Posts" and "Points" values
        postViewModel.getPostsByUserId(userId).observe(viewLifecycleOwner) { posts ->
            val numPosts = posts.size

            // Update the "num_posts" TextView
            val numPostsTextView: TextView = viewFlipper.findViewById(R.id.num_posts)
            numPostsTextView.text = "Posts: $numPosts"

            // Update the "num_points" TextView based on your chosen metric
            val numPoints = numPosts * 25
            val numPointsTextView: TextView = viewFlipper.findViewById(R.id.num_points)
            numPointsTextView.text = "Points: $numPoints"
        }

        initializeAdapter()
        updateDisplayedPosts()
    }

    private fun updateAchievementComponent(numPosts: Int) {
        val firstPostProgressBar = viewFlipper.findViewById<ProgressBar>(R.id.first_post_progressbar)
        val tenPostsProgressBar = viewFlipper.findViewById<ProgressBar>(R.id.ten_posts_progressbar)
        val firstPostText = viewFlipper.findViewById<TextView>(R.id.first_post_text)
        val tenPostsText = viewFlipper.findViewById<TextView>(R.id.ten_posts_text)

        // Update progress bars and text views based on the number of posts
        if (numPosts >= 1) {
            firstPostProgressBar.progress = 100
            firstPostText.text = "Complete!"
        } else {
            firstPostProgressBar.progress = 0
            firstPostText.text = "Progress: $numPosts/1"
        }

        if (numPosts >= 10) {
            tenPostsProgressBar.progress = 100
            tenPostsText.text = "Complete!"
        } else {
            tenPostsProgressBar.progress = (numPosts / 10.0 * 100).toInt()
            tenPostsText.text = "Progress: $numPosts/10"
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

    private fun updateDisplayedPosts() {
        lifecycleScope.launch {
            postViewModel.getPostsByUserId(userId).observe(viewLifecycleOwner) {
                userPostsAdapter.setData(it)
                userPostsAdapter.notifyDataSetChanged()
                userPostsRecyclerView.adapter = userPostsAdapter
            }
        }
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    signUpUser(user)
                } else {
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth.currentUser
    }

    private fun signUpUser(user: FirebaseUser?) {
        val username = user?.displayName ?: ""
        userViewModel.checkUserForLogin(username, "").observe(viewLifecycleOwner) { exists ->
            if (!exists) {
                val newUser = User(username = username)
                userViewModel.insert(newUser).observe(viewLifecycleOwner) { userId ->
                    if (userId != null) {
                        val sharedPreferences =
                            requireActivity().getSharedPreferences("user", 0)
                        val editor = sharedPreferences.edit()
                        editor.putString("userId", userId)
                        editor.apply()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT)
                    .show()
                onSignedIn(user?.displayName.toString())
            }
        }
    }

    private fun onSignedIn(username: String) {
        userViewModel.getIdForUser(username).observe(viewLifecycleOwner) { id ->
            val sharedPreferences = requireActivity().getSharedPreferences("user", 0)
            val editor = sharedPreferences.edit()
            editor.putString("userId", id)
            editor.apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            setUpPage()
        }

    private companion object LoginActivity {
        private const val TAG = "LoginActivity"
    }

}