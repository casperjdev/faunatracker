package com.example.faunatracker.search

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.example.faunatracker.api.MovebankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : BaseActivity() {
    private lateinit var adapter: StudyAdapter
    private var allStudies: List<MovebankRepository.Study> = listOf()

    private lateinit var overlay: FrameLayout
    private lateinit var recycler: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        overlay = findViewById(R.id.loadingOverlay)
        recycler = findViewById(R.id.resultsRecycler)

        searchView = findViewById(R.id.searchBar);
        searchView.isIconified = false
        searchView.requestFocusFromTouch()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)

        adapter = StudyAdapter(listOf())
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        overlay.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            allStudies = MovebankRepository().getStudies()

            withContext(Dispatchers.Main) {
                overlay.visibility = View.GONE
                adapter.updateData(allStudies)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchView.clearFocus()
                    imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterStudies(newText.toString())
                return true
            }
        })
    }

    private fun filterStudies(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allStudies)
            return
        }
        adapter.updateData(
            allStudies.filter {
                it.name.contains(query, ignoreCase = true)

            }
        )
    }
}