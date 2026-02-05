package com.example.faunatracker.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.api.SupabaseRepository
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.auth.session.Session.currentUser
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import com.example.faunatracker.dashboard.DashboardActivity
import com.example.faunatracker.main.MainActivity

class SettingsActivity : BaseActivity() {
    private lateinit var theme: RadioGroup
    private lateinit var notifications: SwitchMaterial
    private lateinit var location: SwitchMaterial

    private lateinit var profileStatic: LinearLayout
    private lateinit var profileEditable: LinearLayout

    private lateinit var unameLabel: TextView
    private lateinit var editButton: MaterialButton
    private lateinit var unameEdit: EditText
    private lateinit var saveButton: MaterialButton

    private lateinit var errorBoundary: TextView
    private lateinit var logoutButton: MaterialButton
    private lateinit var loadingOverlay: FrameLayout

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

        profileStatic = findViewById(R.id.profile_edit_disabled)
        profileEditable = findViewById(R.id.profile_edit_enabled)

        unameLabel = findViewById(R.id.profile_uname)
        editButton = findViewById(R.id.profile_edit)
        unameEdit = findViewById(R.id.profile_uname_edit)
        saveButton = findViewById(R.id.profile_update)

        errorBoundary = findViewById(R.id.profile_error)
        logoutButton = findViewById(R.id.profile_logout)
        loadingOverlay = findViewById(R.id.loadingOverlay)

        currentUser.observe(this) { user ->
            if (user != null) {
                unameLabel.text = user.uname
                unameEdit.setText(user.uname)
            } else {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }

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

        // Changing username
        editButton.setOnClickListener {
            profileStatic.visibility = View.GONE;
            profileEditable.visibility = View.VISIBLE;

            unameEdit.requestFocusFromTouch()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(unameEdit.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }

        saveButton.setOnClickListener {
            val jsonBody = """
                {
                    "uname": "${unameEdit.text}"
                }
            """.trimIndent()

            loadingOverlay.visibility = View.VISIBLE
            errorBoundary.setTextColor("#dd0000".toColorInt())
            errorBoundary.text = ""

            lifecycleScope.launch {
                try {
                    val res = SupabaseRepository().updateUser(
                        currentUser.value?.id!!, jsonBody)
                    loadingOverlay.visibility = View.GONE

                    if (res) {
                        profileStatic.visibility = View.VISIBLE;
                        profileEditable.visibility = View.GONE;

                        Session.refresh()

                        errorBoundary.setTextColor("#00dd00".toColorInt())
                        errorBoundary.text = "Username updated!"
                    } else {
                        errorBoundary.setTextColor("#dd0000".toColorInt())
                        errorBoundary.text = "Could not update username."
                    }
                } catch (e: Exception) {
                    loadingOverlay.visibility = View.GONE
                    errorBoundary.text = "Could not update username."
                }
            }
        }

        // Logging out
        logoutButton.setOnClickListener {
            Session.logout()
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