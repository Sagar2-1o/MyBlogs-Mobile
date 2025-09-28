package com.myblogs.Fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.myblogs.DataBase.Constants
import com.myblogs.DataBase.UserData
import com.myblogs.R
import com.myblogs.databinding.FragmentLoginBinding
import com.myblogs.databinding.FragmentSignupBinding
import java.util.UUID

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        binding.btnsignin.setOnClickListener {
            performSignup()
        }

        return binding.root
    }

    private fun performSignup() {
        val username = binding.etusername.text.toString().trim()
        val email = binding.etemail.text.toString().trim()
        val password = binding.etpassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.etusername.error = "Enter username"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etemail.error = "Enter valid email"
            return
        }

        if (password.length < 6) {
            binding.etpassword.error = "Password must be at least 6 characters"
            return
        }

        // Check if email already exists
        val query = userRef.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(context, "Email already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    // Generate unique user ID
                    val userId = UUID.randomUUID().toString()

                    val userData = UserData(
                        userId = userId,
                        name = username,
                        email = email,
                        password = password,
                        profileImageUrl = "" // Optional, can be updated later
                    )

                    // Save user data
                    userRef.child(userId).setValue(userData)
                        .addOnSuccessListener {

                            // ✅ Save login session after signup
                            val sharedPref = requireContext().getSharedPreferences("MyBlogPrefs", android.content.Context.MODE_PRIVATE)
                            sharedPref.edit()
                                .putBoolean(Constants.isLoggedIn, true)  // Session true
                                .putString(Constants.userId, userId)     // Save userId
                                .apply()

                            Toast.makeText(context, "Signup successful!", Toast.LENGTH_SHORT).show()

                            // ✅ Navigate to HomeFragment
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.mainfragment, HomeFragment())
                                .commit()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}


