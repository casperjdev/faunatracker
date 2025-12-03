package com.example.faunatracker.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.example.faunatracker.R
import com.example.faunatracker.auth.session.Session.currentUser
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.search.SearchActivity
import com.example.faunatracker.settings.SettingsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        currentUser.observe(this) { user ->
            if (user != null) {
                val greeting = findViewById<TextView>(R.id.dashboard_greeting)
                greeting.text = "Welcome, ${user.email}"
            }
        }

        val searchButton = findViewById<FloatingActionButton>(R.id.dashboard_search)

        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<ImageButton>(R.id.nav_settings)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}