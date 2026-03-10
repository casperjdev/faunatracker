package com.example.faunatracker.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.faunatracker.R
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.dashboard.DashboardActivity
import com.example.faunatracker.databinding.ActivityAuthBinding

class AuthActivity : BaseActivity<ActivityAuthBinding>(ActivityAuthBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            initAuth()
        }
        observeUser()
    }

    private fun initAuth() {
        supportFragmentManager.beginTransaction()
            .add(R.id.auth_fragment_container, LoginFragment())
            .commit()
    }

    private fun observeUser() {
        Session.currentUser.observe(this) { user ->
            if (user != null) {
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }
}