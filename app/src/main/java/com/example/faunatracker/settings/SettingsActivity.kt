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
import com.example.faunatracker.databinding.ActivitySettingsBinding
import com.example.faunatracker.main.MainActivity

class SettingsActivity : BaseActivity<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {
    private val prefs by lazy {
        getSharedPreferences("app_settings", MODE_PRIVATE)
    }
    private val repo = SupabaseRepository()

    companion object {
        private const val REQUEST_CODE_LOCATION = 1001
        private const val REQUEST_CODE_NOTIFICATIONS = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSettings()
        observeUser()
        setupListeners()
    }

    private fun loadSettings() {
        with(binding) {
            when (prefs.getString("theme", "Light")) {
                "Light" -> settingsTheme.check(themeLight.id)
                "Dark" -> settingsTheme.check(themeDark.id)
            }

            settingsNotifications.isChecked = prefs.getBoolean("notifications", true)
            settingsLocation.isChecked = prefs.getBoolean("location", false)
        }
    }

    private fun observeUser() {
        with(binding) {
            currentUser.observe(this@SettingsActivity) { user ->
                if (user != null) {
                    profileUname.text = user.uname
                    profileUnameEdit.setText(user.uname)
                } else {
                    val intent = Intent(this@SettingsActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
            }
        }

    }

    private fun setupListeners() {
        with(binding) {
            // theme
            settingsTheme.setOnCheckedChangeListener { _, checkedId ->
                val selected = when (checkedId) {
                    themeLight.id -> "Light"
                    themeDark.id -> "Dark"
                    else -> "Light"
                }
                prefs.edit { putString("theme", selected) }

                when (selected) {
                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }

            // Notifications
            settingsNotifications.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("notifications", isChecked) }

                if (isChecked) {
                    requestNotificationPermissionIfNeeded()
                } else {
                    disableNotifications()
                }
            }

            // Location
            settingsLocation.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit { putBoolean("location", isChecked) }

                if (isChecked) {
                    requestLocationPermissionIfNeeded()
                } else {
                    stopLocationUpdates()
                }
            }

            // Changing username
            profileEdit.setOnClickListener {
                profileEditDisabled.visibility = View.GONE;
                profileEditEnabled.visibility = View.VISIBLE;

                profileUnameEdit.requestFocusFromTouch()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(profileUnameEdit.findFocus(), InputMethodManager.SHOW_IMPLICIT)
            }

            profileUpdate.setOnClickListener {
                val jsonBody = """
                {
                    "uname": "${profileUnameEdit.text}"
                }
            """.trimIndent()

                loadingOverlay.visibility = View.VISIBLE

                profileError.apply {
                    setTextColor("#dd0000".toColorInt())
                    text = ""
                }

                lifecycleScope.launch {
                    try {
                        val user = currentUser.value ?: return@launch
                        val res = repo.updateUser(user.id, jsonBody)
                        loadingOverlay.visibility = View.GONE

                        if (res) {
                            profileUname.visibility = View.VISIBLE;
                            profileUnameEdit.visibility = View.GONE;

                            Session.refresh()

                            profileError.apply {
                                setTextColor("#00dd00".toColorInt())
                                text = "Username updated!"
                            }
                        } else {
                            profileError.apply {
                                setTextColor("#dd0000".toColorInt())
                                text = "Could not update username."
                            }
                        }
                    } catch (e: Exception) {
                        loadingOverlay.visibility = View.GONE
                        profileError.text = "Could not update username."
                    }
                }
            }

            // Logging out
            profileLogout.setOnClickListener {
                Session.logout()
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