package com.cmpt362.blissful.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<Post>
    private lateinit var adapter: GratitudeAdapter

    // DB instances
    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: PostDatabaseDao
    private lateinit var repository: PostRepository
    private lateinit var viewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    private lateinit var recyclerView: RecyclerView

    private fun initializeDatabase() {
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
    }

    private fun initializeAdapter(root: View) {
        recyclerView = root.findViewById(R.id.home_gratitude_messages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        arrayList = ArrayList()
        adapter = GratitudeAdapter(arrayList)
        recyclerView.adapter = adapter
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

        postViewModel.allPublicPosts.observe(viewLifecycleOwner) {
            Log.d("HomeFragment", "Gratitude items changed: $it")
            adapter.setData(it)
            adapter.notifyDataSetChanged()
            recyclerView.adapter = adapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun redirectToAdd() {
        val navController = findNavController()
        navController.popBackStack()
        findNavController().navigate(R.id.navigation_add)
    }
}