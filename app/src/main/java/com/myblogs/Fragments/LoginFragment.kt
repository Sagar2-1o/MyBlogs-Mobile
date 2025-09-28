package com.myblogs.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.myblogs.DataBase.Constants
import com.myblogs.DataBase.UserData
import com.myblogs.R
import com.myblogs.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var sharedpref : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)


        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("users")

        setOnClicks()
        setupClickableSpans()

        return binding.root
    }

    // ------------------------- Clickable "Sign up" -------------------------
    private fun setupClickableSpans() {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val fragment = SignupFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainfragment, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }

        val text = "Don't have an account? Sign up"
        val spanText = "Sign up"
        val index = text.indexOf(spanText)
        val spannableString = SpannableString(text)

        spannableString.setSpan(
            clickableSpan,
            index,
            index + spanText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvnoaccount.text = spannableString
        binding.tvnoaccount.movementMethod = LinkMovementMethod.getInstance()
    }

    // ------------------------- Button Clicks -------------------------
    private fun setOnClicks() {

        // Login button
        binding.btnsignin.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            performLogin()
        }

        // Forgot password
        binding.tvforgot.setOnClickListener {
            val fragment = ForgotFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    // ------------------------- Input Validations -------------------------
    private fun validateInputs(): Boolean {
        val email = binding.etemail.text?.trim().toString()
        val password = binding.etpassword.text?.trim().toString()

        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter your Email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid Email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(context, "Please enter your Password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ------------------------- Perform Login -------------------------
    private fun performLogin() {
        val emailInput = binding.etemail.text?.trim().toString()
        val passwordInput = binding.etpassword.text?.trim().toString()

        val query = userRef.orderByChild("email").equalTo(emailInput)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var userFound: UserData? = null
                    for (userSnap in snapshot.children) {
                        val user = userSnap.getValue(UserData::class.java)
                        if (user != null && user.password == passwordInput) {
                            userFound = user
                            break
                        }
                    }

                    if (userFound != null) {
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

                        // ✅ Save login session using SharedPreferences
                        val sharedPref = requireContext().getSharedPreferences("MyBlogPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit()
                            .putBoolean(Constants.isLoggedIn, true) // <-- Set login session true
                            .putString(Constants.userId, userFound.userId) // Optional: Save userId
                            .apply()

                        // ✅ Navigate to HomeFragment
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.mainfragment, HomeFragment())
                            .commit()
                    } else {
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
