package com.myblogs

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.myblogs.DataBase.Constants
import com.myblogs.Fragments.HomeFragment
import com.myblogs.Fragments.LoginFragment
import com.myblogs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedpref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedpref = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        isUserLogin()
    }

    private fun isUserLogin() {
        if (sharedpref.getBoolean(Constants.isLoggedIn, false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, HomeFragment())
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainfragment, LoginFragment())
                .commit()
        }
    }
}
