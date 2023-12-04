package com.cmpt362.blissful.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentHomeBinding
import com.cmpt362.blissful.db.like.Like
import com.cmpt362.blissful.db.like.LikeRepository
import com.cmpt362.blissful.db.like.LikeViewModel
import com.cmpt362.blissful.db.like.LikeViewModelFactory
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // DB instances
    private lateinit var postViewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    private lateinit var likesViewModelFactory: LikeViewModelFactory
    private lateinit var likeViewModel: LikeViewModel

    private lateinit var publicPostsArrayList: ArrayList<Post>
    private lateinit var publicPostsAdapter: PostAdapter
    private lateinit var publicPostsRecyclerView: RecyclerView

    // Logged in state
    private var userId: String = ""
    private var isSignedIn: Boolean = false
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            getCredentials()
            updateDisplayedPosts()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

    private fun initializeDatabase() {
        val firebase = FirebaseFirestore.getInstance()
        val postRepository = PostRepository(firebase)
        postViewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, postViewModelFactory)[PostViewModel::class.java]

        val likeRepository = LikeRepository(firebase)
        likesViewModelFactory = LikeViewModelFactory(likeRepository)
        likeViewModel = ViewModelProvider(this, likesViewModelFactory)[LikeViewModel::class.java]
    }

    private fun initializeAdapter(root: View) {
        publicPostsRecyclerView = root.findViewById(R.id.public_posts)
        publicPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        publicPostsArrayList = ArrayList()
        publicPostsAdapter = PostAdapter(publicPostsArrayList, setOf(), ::onHeartToggled)
        publicPostsRecyclerView.adapter = publicPostsAdapter
        publicPostsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun getCredentials() {
        userId = getUserId(requireContext())
        isSignedIn = userId != ""
    }

    private fun updateDisplayedPosts() {
        if (isSignedIn) {
            lifecycleScope.launch {
                postViewModel.getPostsWithoutUserId(userId).observe(viewLifecycleOwner) {
                    publicPostsAdapter.setData(it)
                    publicPostsAdapter.notifyDataSetChanged()
                    publicPostsRecyclerView.adapter = publicPostsAdapter
                }
            }
            lifecycleScope.launch {
                likeViewModel.getLikesByUserId(userId).observe(viewLifecycleOwner) {
                    publicPostsAdapter.setUserLikedPosts(it)
                    publicPostsAdapter.notifyDataSetChanged()
                }
            }
        } else {
            lifecycleScope.launch {
                postViewModel.getPublicPosts().observe(viewLifecycleOwner) {
                    publicPostsAdapter.setData(it)
                    publicPostsAdapter.notifyDataSetChanged()
                    publicPostsRecyclerView.adapter = publicPostsAdapter
                }
            }
        }
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

    private fun onHeartToggled(postId: String, heartToggle: ToggleButton, likesCount: TextView) {
        if (isSignedIn) {
            if (heartToggle.isChecked) {
                likesCount.text = (likesCount.text.toString().toInt() + 1).toString()
                likeViewModel.insert(Like(userId = userId, postId = postId))
                postViewModel.likePost(postId)
            } else {
                likesCount.text = (likesCount.text.toString().toInt() - 1).toString()
                likeViewModel.delete(userId, postId)
                postViewModel.unlikePost(postId)
            }
        } else {
            heartToggle.isChecked = false
            Toast.makeText(requireContext(), "Please sign in to like posts", Toast.LENGTH_SHORT)
                .show()
        }
    }
}