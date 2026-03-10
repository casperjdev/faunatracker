package com.example.faunatracker.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.faunatracker.R
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.api.SupabaseRepository
import com.example.faunatracker.auth.session.Session.currentUser
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.databinding.ActivityDashboardBinding
import com.example.faunatracker.main.MainActivity
import com.example.faunatracker.search.SearchActivity
import com.example.faunatracker.settings.SettingsActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : BaseActivity<ActivityDashboardBinding>(ActivityDashboardBinding::inflate) {
    private var studies: MutableList<MovebankRepository.Study> = mutableListOf()
    private var adapter = StudyAdapter(mutableListOf())
    private val repo = MovebankRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeUser()
        setupListeners()
    }

    private fun observeUser() {
        with(binding) {
            currentUser.observe(this@DashboardActivity) { user ->
                if (user != null) {
                    dashboardGreeting.text = "Welcome, ${user.uname}"

                    // Clear old data to avoid duplicates
                    studies = mutableListOf()

                    dashboardList.apply {
                        adapter = this@DashboardActivity.adapter
                        layoutManager = LinearLayoutManager(this@DashboardActivity)
                    }

                    if (user.saved_studies.isNotEmpty()) {
                        dashboardPlaceholder.visibility = View.GONE
                        loadingOverlay.visibility = View.VISIBLE
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        user.saved_studies.forEach {
                            val study = repo.getSingleStudy(it)
                            studies.add(study)
                        }

                        withContext(Dispatchers.Main) {
                            loadingOverlay.visibility = View.GONE
                            adapter.updateData(studies)
                            updateEmptyState(user)
                        }
                    }

                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                    searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            if (!query.isNullOrBlank()) {
                                searchBar.clearFocus()
                                imm.hideSoftInputFromWindow(searchBar.windowToken, 0)
                            }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            filterStudies(newText.toString())
                            return true
                        }
                    })
                } else {
                    val intent = Intent(this@DashboardActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
            }
        }
    }


    private fun setupListeners() {
        with(binding) {
            dashboardSearch.setOnClickListener {
                val intent = Intent(this@DashboardActivity, SearchActivity::class.java)
                startActivity(intent)
            }

            navSettings.setOnClickListener {
                val intent = Intent(this@DashboardActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun filterStudies(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(studies)
            return
        }
        adapter.updateData(
            studies.filter {
                it.name.contains(query, ignoreCase = true)
                        || it.taxon_ids.split(",")[0].contains(query, ignoreCase = true)
            }
        )
    }

    private fun updateEmptyState(user: SupabaseRepository.User) {
        with(binding) {
            if (user.saved_studies.isEmpty()) {
                dashboardPlaceholder.visibility = View.VISIBLE
                dashboardListContainer.visibility = View.GONE
            } else {
                dashboardPlaceholder.visibility = View.GONE
                dashboardListContainer.visibility = View.VISIBLE
            }
        }
    }
}