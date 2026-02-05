package com.example.faunatracker.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.faunatracker.R
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.auth.session.Session.currentUser
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.search.SearchActivity
import com.example.faunatracker.settings.SettingsActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : BaseActivity() {
    private lateinit var greeting: TextView
    private lateinit var searchButton: MaterialButton
    private lateinit var settingsButton: ImageButton

    private var studies: MutableList<MovebankRepository.Study> = mutableListOf()
    private lateinit var adapter: StudyAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var recyclerContainer: LinearLayout
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var emptyShell: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        currentUser.observe(this) { user ->
            if (user != null) {
                greeting = findViewById(R.id.dashboard_greeting)
                greeting.text = "Welcome, ${user.uname}"

                emptyShell = findViewById(R.id.dashboard_placeholder)
                recycler = findViewById(R.id.dashboard_list)
                recyclerContainer = findViewById(R.id.dashboard_list_container)
                loadingOverlay = findViewById(R.id.loadingOverlay)

                if (!user.saved_studies.isEmpty()) {
                    adapter = StudyAdapter(listOf())
                    recycler.adapter = adapter
                    recycler.layoutManager = LinearLayoutManager(this)

                    emptyShell.visibility = View.GONE
                    loadingOverlay.visibility = View.VISIBLE

                    lifecycleScope.launch(Dispatchers.IO) {
                        user.saved_studies.forEach {
                            val study = MovebankRepository().getSingleStudy(it)
                            studies.add(study)
                        }

                        withContext(Dispatchers.Main) {
                            loadingOverlay.visibility = View.GONE
                            adapter.updateData(studies)
                            recyclerContainer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        searchButton = findViewById(R.id.dashboard_search)
        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        settingsButton = findViewById(R.id.nav_settings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}