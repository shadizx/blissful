package com.cmpt362.blissful.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentHomeBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getUserId

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var publicPostsArrayList: ArrayList<Post>
    private lateinit var publicPostsAdapter: GratitudeAdapter
    private lateinit var userPostsArrayList: ArrayList<Post>
    private lateinit var userPostsAdapter: GratitudeAdapter
    private lateinit var userPostsHeader: TextView

    // DB instances
    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: PostDatabaseDao
    private lateinit var repository: PostRepository
    private lateinit var viewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    private lateinit var publicPostsRecyclerView: RecyclerView
    private lateinit var userPostsRecyclerView: RecyclerView

    // Logged in state
    private var userId: Int = -1
    private var isSignedIn: Boolean = false
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            updateDisplayedPosts()
        }


    private fun initializeDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
    }

    private fun initializeAdapter(root: View) {
        // public posts
        publicPostsRecyclerView = root.findViewById(R.id.public_posts)
        publicPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        publicPostsArrayList = ArrayList()
        publicPostsAdapter = GratitudeAdapter(publicPostsArrayList)
        publicPostsRecyclerView.adapter = publicPostsAdapter
        publicPostsRecyclerView.isNestedScrollingEnabled = false

        // user posts
        userPostsRecyclerView = root.findViewById(R.id.user_posts)
        userPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userPostsArrayList = ArrayList()
        userPostsAdapter = GratitudeAdapter(userPostsArrayList)
        userPostsRecyclerView.adapter = userPostsAdapter
        userPostsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != -1
    }

    private fun updateDisplayedPosts() {
        if (isSignedIn) {
            postViewModel.getPostsByUserId(userId).observe(viewLifecycleOwner) {
                userPostsAdapter.setData(it)
                userPostsAdapter.notifyDataSetChanged()
                userPostsRecyclerView.adapter = userPostsAdapter
            }

            postViewModel.getPostsWithoutUserId(userId).observe(viewLifecycleOwner) {
                publicPostsAdapter.setData(it)
                publicPostsAdapter.notifyDataSetChanged()
                publicPostsRecyclerView.adapter = publicPostsAdapter
            }
        } else {
            postViewModel.allPublicPosts.observe(viewLifecycleOwner) {
                publicPostsAdapter.setData(it)
                publicPostsAdapter.notifyDataSetChanged()
                publicPostsRecyclerView.adapter = publicPostsAdapter
            }
            userPostsHeader.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeDatabase()
        initializeAdapter(root)
        userPostsHeader = root.findViewById(R.id.user_posts_header)

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