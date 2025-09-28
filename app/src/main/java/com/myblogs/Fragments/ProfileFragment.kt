package com.myblogs.Fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.myblogs.DataBase.Constants
import com.myblogs.DataBase.UserData
import com.myblogs.R
import com.myblogs.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var database: FirebaseDatabase
    private var currentUserId: String? = null

    private val PICK_IMAGE_REQUEST = 100
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance()

        // --- Get current user ID from SharedPreferences ---
        val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, 0)
        currentUserId = sharedPref.getString(Constants.userId, null)

        fetchUserData()
        setClickListeners()

        return binding.root
    }

    private fun fetchUserData() {
        if (currentUserId == null) return

        database.reference.child("users").child(currentUserId!!)
            .get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserData::class.java)
                if (user != null) {
                    binding.etname.setText(user.name)
                    binding.etemail.setText(user.email)

                    // Optional: load profile image if URL exists
                    if (user.profileImageUrl.isNotEmpty()) {
                        binding.ivImage.setImageURI(Uri.parse(user.profileImageUrl))
                    }
                }
            }
    }

    private fun setClickListeners() {
        binding.ivsettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, SettingFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.llhome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.fbtnadd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, NewBlogFragment())
                .addToBackStack(null)
                .commit()
        }

        // --- Upload Profile Picture ---
        binding.btnuploadimage.setOnClickListener {
            checkPermissionAndOpenGallery()
        }
    }

    // --- Permission check for gallery ---
    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            openGallery()
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    101
                )
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(context, "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                binding.ivImage.setImageURI(it)
            }
        }
    }
}
