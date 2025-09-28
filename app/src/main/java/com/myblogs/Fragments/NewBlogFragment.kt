package com.myblogs.Fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.myblogs.DataBase.Constants
import com.myblogs.DataBase.NewBlogData
import com.myblogs.R
import com.myblogs.databinding.FragmentNewBlogBinding
import java.io.ByteArrayOutputStream
import java.util.UUID
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.FirebaseDatabase.getInstance

class NewBlogFragment : Fragment() {

    private lateinit var binding: FragmentNewBlogBinding
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 101
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        binding = FragmentNewBlogBinding.inflate(layoutInflater)

        // Get current userId and userName from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, 0)
        currentUserId = sharedPref.getString(Constants.userId, null)
        currentUserName = sharedPref.getString(Constants.userName, "Anonymous") // default name

        setClickListeners()
        return binding.root
    }

    private fun setClickListeners() {
        // Navigate back on card click
        binding.cvinmagecard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // Upload image
        binding.btnuplaodimage.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        // Submit blog
        binding.btnsubmit.setOnClickListener {
            submitBlog()
        }
    }

    // --------------------- Permissions & Gallery ---------------------
    private fun checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    PICK_IMAGE_REQUEST
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
        if (requestCode == PICK_IMAGE_REQUEST && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            Toast.makeText(context, "Permission denied to access gallery", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.ivPreviewImage.setImageURI(selectedImageUri)
        }
    }

    // --------------------- Submit Blog ---------------------
    private fun submitBlog() {
        val title = binding.etname.text.toString().trim()
        val description = binding.etdescription.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            binding.etname.error = "Enter title"
            return
        }

        if (TextUtils.isEmpty(description)) {
            binding.etdescription.error = "Enter description"
            return
        }

        if (currentUserId == null) {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val blogId = UUID.randomUUID().toString()
        var imageBase64 = ""

        // Convert selected image to Base64 string
        if (selectedImageUri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val byteArray = baos.toByteArray()
                imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Image conversion failed", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val blogData = NewBlogData(
            blogId = blogId,
            userId = currentUserId!!,
            userName = currentUserName!!, // Added field
            title = title,
            description = description,
            imageUrl = imageBase64
        )

        // Save blog to Firebase Realtime Database
        database.reference.child("blogs").child(blogId)
            .setValue(blogData)
            .addOnSuccessListener {
                Toast.makeText(context, "Blog published successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainfragment, HomeFragment())
                    .commit()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
