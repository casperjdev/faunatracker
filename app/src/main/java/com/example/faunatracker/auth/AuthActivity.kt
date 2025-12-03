package com.example.faunatracker.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.faunatracker.R
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.dashboard.DashboardActivity

class AuthActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.auth_fragment_container, LoginFragment())
                .commit()
        }

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