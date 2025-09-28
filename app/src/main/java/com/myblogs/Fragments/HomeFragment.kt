package com.myblogs.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import com.myblogs.Adapters.BlogAdapter
import com.myblogs.DataBase.NewBlogData
import com.myblogs.R
import com.myblogs.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var blogAdapter: BlogAdapter
    private val blogList = mutableListOf<NewBlogData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference.child("blogs")

        setupRecyclerView()
        fetchBlogs()
        setClicks()

        return binding.root
    }

    private fun setupRecyclerView() {
        blogAdapter = BlogAdapter(requireContext(), blogList)
        binding.rlView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rlView.adapter = blogAdapter
    }

    private fun fetchBlogs() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogList.clear()
                for (snap in snapshot.children) {
                    val blog = snap.getValue(NewBlogData::class.java)
                    blog?.let { blogList.add(it) }
                }
                if (blogList.isEmpty()) {
                    Toast.makeText(context, "No blogs found", Toast.LENGTH_SHORT).show()
                }
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load blogs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setClicks() {
        binding.cvinmagecard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.llprofile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.fbtnadd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, NewBlogFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
