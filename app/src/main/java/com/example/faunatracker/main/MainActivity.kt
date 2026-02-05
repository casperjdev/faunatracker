package com.example.faunatracker.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.auth.AuthActivity
import com.example.faunatracker.R
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.base.BaseActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("theme", "Light") ?: "Light"
        when (theme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        button = findViewById(R.id.hero_button)
        button.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}