package com.myblogs.Fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.myblogs.DataBase.Constants
import com.myblogs.R
import com.myblogs.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private val database = FirebaseDatabase.getInstance()
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(layoutInflater)

        // --- Get current user ID from SharedPreferences ---
        val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, 0)
        currentUserId = sharedPref.getString(Constants.userId, null)

        setClickListeners()
        return binding.root
    }

    private fun setClickListeners() {

        // --- Logout button ---
        binding.btnlogout.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, 0)
            sharedPref.edit().clear().apply()  // ✅ Clear session

            Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()

            parentFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, LoginFragment())
                .commit()
        }

        // --- Delete Account button ---
        binding.btndeleteaccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteUserAccount()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // --- Image card click (go back) ---
        binding.cvinmagecard.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun deleteUserAccount() {
        if (currentUserId == null) {
            Toast.makeText(context, "User ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        // Delete user from Firebase
        database.reference.child("users").child(currentUserId!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Account deleted successfully!", Toast.LENGTH_SHORT).show()

                // ✅ Clear session after deleting account
                val sharedPref = requireContext().getSharedPreferences(Constants.PREF_NAME, 0)
                sharedPref.edit().clear().apply()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainfragment, SignupFragment())
                    .commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
