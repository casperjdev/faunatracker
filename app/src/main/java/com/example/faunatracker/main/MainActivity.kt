package com.example.faunatracker.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.faunatracker.auth.AuthActivity
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val prefs by lazy {
        getSharedPreferences("app_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTheme()
        setupListeners()
    }

    private fun setupTheme() {
        val theme = prefs.getString("theme", "Light") ?: "Light"
        when (theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun setupListeners() = with(binding) {
        heroButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}