package com.cmpt362.blissful.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.databinding.FragmentAddBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import java.util.Calendar

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var submitButton: Button
    private lateinit var postTextView: EditText

    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: PostDatabaseDao
    private lateinit var repository: PostRepository
    private lateinit var viewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]

        postTextView = binding.postInput
        submitButton = binding.submitButton
        submitButton.setOnClickListener {
            submitPost()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submitPost() {
        val userId = getUserId(requireContext())
        if (userId == -1) {
            Toast.makeText(
                requireContext(),
                "Please sign in to submit a post",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val postText = postTextView.text.toString()
            if (postText.isNotEmpty()) {
                val post = Post(
                    userId = userId,
                    content = postText,
                    location = "",
                )
                postViewModel.insert(post)
                postTextView.text.clear()
                Toast.makeText(requireContext(), "Post submitted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}