package com.example.faunatracker.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.content.edit

class SettingsActivity : BaseActivity() {
    private lateinit var theme: RadioGroup
    private lateinit var notifications: SwitchMaterial
    private lateinit var location: SwitchMaterial

    private val prefs by lazy {
        getSharedPreferences("app_settings", MODE_PRIVATE)
    }

    companion object {
        private const val REQUEST_CODE_LOCATION = 1001
        private const val REQUEST_CODE_NOTIFICATIONS = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        theme = findViewById(R.id.settings_theme)
        notifications = findViewById(R.id.settings_notifications)
        location = findViewById(R.id.settings_location)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        // Theme
        when (prefs.getString("theme", "Light")) {
            "Light" -> theme.check(R.id.theme_light)
            "Dark" -> theme.check(R.id.theme_dark)
        }

        // Notifications
        notifications.isChecked = prefs.getBoolean("notifications", true)

        // Location
        location.isChecked = prefs.getBoolean("location", false)
    }

    private fun setupListeners() {
        // theme
        theme.setOnCheckedChangeListener { _, checkedId ->
            val selected = when (checkedId) {
                R.id.theme_light -> "Light"
                R.id.theme_dark -> "Dark"
                else -> "Light"
            }
            prefs.edit { putString("theme", selected) }

            when (selected) {
                "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // Notifications
        notifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("notifications", isChecked) }

            if (isChecked) {
                requestNotificationPermissionIfNeeded()
            } else {
                disableNotifications()
            }
        }

        // Location
        location.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean("location", isChecked) }

            if (isChecked) {
                requestLocationPermissionIfNeeded()
            } else {
                stopLocationUpdates()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATIONS
            )
        } else {
            enableNotifications()
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        // ...
    }

    private fun stopLocationUpdates() {
        // ...
    }

    private fun enableNotifications() {
        // ...
    }

    private fun disableNotifications() {
        // ...
    }



}