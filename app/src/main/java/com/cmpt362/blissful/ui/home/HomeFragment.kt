package com.cmpt362.blissful.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentHomeBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // DB instances
    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: PostDatabaseDao
    private lateinit var repository: PostRepository
    private lateinit var viewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    private lateinit var publicPostsRecyclerView: RecyclerView

    // Logged in state
    private var userId: Int = -1
    private var isSignedIn: Boolean = false
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            updateDisplayedPosts()
        }

    // Firebase
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: FirestoreRecyclerAdapter<User, UserViewHolder>
    private val storageRef = Firebase.storage.reference

    data class User(
        val entryName: String = "",
        val data: String = ""
    )

    private fun initializeDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
    }

    private fun initializeAdapter(root: View) {
        publicPostsRecyclerView = root.findViewById(R.id.public_posts)
        publicPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        publicPostsRecyclerView.adapter = adapter
        publicPostsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != -1
    }

    private fun updateDisplayedPosts() {
        if (isSignedIn) {
            postViewModel.getPostsWithoutUserId(userId).observe(viewLifecycleOwner) {
                publicPostsRecyclerView.adapter = adapter
            }
        } else {
            postViewModel.allPublicPosts.observe(viewLifecycleOwner) {
                publicPostsRecyclerView.adapter = adapter
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = Firebase.auth

        // Query the users collection
        val query = db.collection("entries")

        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
            .setLifecycleOwner(this).build()
        adapter = object : FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.home_gratitude_list_item, parent, false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                val name: TextView = holder.itemView.findViewById(R.id.itemDescription)
                val message: TextView = holder.itemView.findViewById(R.id.authorUsername)
                val imageView: ImageView = holder.itemView.findViewById(R.id.itemImage)
                imageView.visibility = View.VISIBLE
                if (model.data != "") {
                    // using glide library to display the image
                    storageRef.child("file/${model.data}").downloadUrl.addOnSuccessListener {
                        Glide.with(requireActivity())
                            .load(it)
                            .into(imageView)
                        Log.e("Firebase", "download passed")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Failed in downloading")

                    }
                } else {
                    // Disable imageview if no image was passed by the user
                    imageView.visibility = View.GONE

                }

                name.text = model.data
                message.text = model.entryName
            }
        }

        initializeDatabase()
        initializeAdapter(root)

        val redirectButton: Button = root.findViewById(R.id.home_redirect_button)
        redirectButton.setOnClickListener {
            redirectToAdd()
        }

        getCredentials()
        updateDisplayedPosts()
        requireActivity().getSharedPreferences("user", 0)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().getSharedPreferences("user", 0)
            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        _binding = null
    }

    private fun redirectToAdd() {
        val navController = findNavController()
        navController.popBackStack()
        findNavController().navigate(R.id.navigation_add)
    }

}