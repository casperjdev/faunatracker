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
import com.example.faunatracker.databinding.ActivitySearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {
    private var studies: List<MovebankRepository.Study> = listOf()
    private var adapter = StudyAdapter(mutableListOf())
    private val repo = MovebankRepository()
    private lateinit var imm: InputMethodManager
    private lateinit var query: String

    companion object {
        const val EXTRA_QUERY = "EXTRA_QUERY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        setupSearch()
        setupListeners()
    }

    private fun setupSearch() = with(binding) {
        query = intent.getStringExtra(EXTRA_QUERY) ?: ""

        searchBar.isIconified = false
        searchBar.clearFocus()

        resultsRecycler.apply {
            adapter = this@SearchActivity.adapter
            layoutManager = LinearLayoutManager(this@SearchActivity)
        }

        loadingOverlay.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            studies = repo.getStudies()

            withContext(Dispatchers.Main) {
                loadingOverlay.visibility = View.GONE
                adapter.updateData(studies)

                searchBar.requestFocus()
                imm.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)

                searchBar.setQuery(query, false)
            }


        }
    }

    private fun setupListeners() = with(binding) {
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
}