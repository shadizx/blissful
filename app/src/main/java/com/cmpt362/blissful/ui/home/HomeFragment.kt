package com.cmpt362.blissful.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<GratitudeItem>
    private lateinit var adapter: GratitudeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = root.findViewById(R.id.home_gratitude_messages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        arrayList = ArrayList()
        adapter = GratitudeAdapter(arrayList)
        recyclerView.adapter = adapter

        val redirectButton: Button = root.findViewById(R.id.home_redirect_button)
        redirectButton.setOnClickListener {
            redirectToAdd()
        }

        homeViewModel.gratitudeItems.observe(viewLifecycleOwner) {
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