package com.example.faunatracker.map

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.search.api.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapActivity : BaseActivity() {
    // SHEET
    private lateinit var bottomSheet: LinearLayout
    private lateinit var behavior: BottomSheetBehavior<LinearLayout>
    private lateinit var thumb: View
    private lateinit var headerRow: View

    private var hiddenHeight = 0
    private var halfHeight = 0
    private var lastSlideOffset = 0f
    private var slideDirection = SlideDirection.NONE

    enum class SlideDirection { UP, DOWN, NONE }

    // DATA
    companion object {
        const val EXTRA_STUDY_ID = "EXTRA_STUDY_ID"
    }
    private var studyId: String? = null
    private var study: SearchRepository.Study? = null // your study model
    private lateinit var navbarTitle: TextView
    private lateinit var speciesImage: ImageView
    private lateinit var speciesName: TextView
    private lateinit var speciesContent: TextView

    // MAP
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // SHEET
        bottomSheet = findViewById(R.id.bottomSheet)

        ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { v, insets ->
            v.setPadding(0,0,0,0)

            val lp = v.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = 0
            v.layoutParams = lp

            insets
        }

        thumb = findViewById(R.id.thumb)
        headerRow = findViewById(R.id.headerRow)

        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isFitToContents = false
        behavior.skipCollapsed = false
        behavior.isDraggable = true
        behavior.expandedOffset = 0


        bottomSheet.post {
            setupBreakpoints()
        }
        attachCallbacks()

        // DATA
        studyId = intent.getStringExtra(EXTRA_STUDY_ID)
        navbarTitle = findViewById(R.id.navbarText)
        speciesImage = findViewById(R.id.speciesImage)
        speciesName = findViewById(R.id.speciesName)
        speciesContent = findViewById(R.id.wikiContent)

        // Load study details (from your repository)
        studyId?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                study = SearchRepository().getSingleStudy(it)

                withContext(Dispatchers.Main) {
                    study?.let{ study ->
                        navbarTitle.text = study.name
                        speciesName.text = if (!study.taxon_ids.isEmpty()) study.taxon_ids.split(",").getOrNull(0) else "Unknown Species"
                        speciesImage.setImageResource(R.drawable.ic_launcher_background)
                        speciesContent.text = "Loading Wikipedia content..."
                    }
                }
            }
        }
    }

    // HELPERS FOR SHEET
    private fun setupBreakpoints() {
        hiddenHeight = thumb.height
        halfHeight = thumb.height + headerRow.height

        val parentHeight = (bottomSheet.parent as View).height

        behavior.peekHeight = hiddenHeight
        behavior.halfExpandedRatio = halfHeight.toFloat() / parentHeight
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun attachCallbacks() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val delta = slideOffset - lastSlideOffset
                lastSlideOffset = slideOffset

                slideDirection = when {
                    delta > 0f -> SlideDirection.UP
                    delta < 0f -> SlideDirection.DOWN
                    else -> SlideDirection.NONE
                }
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_SETTLING) {
                    // Snap to next state based on drag direction
                    when (slideDirection) {
                        SlideDirection.UP -> goToNextHigherState()
                        SlideDirection.DOWN -> goToNextLowerState()
                        else -> return
                    }
                }
            }
        })
    }

    private fun goToNextHigherState() {
        when (behavior.state) {
            BottomSheetBehavior.STATE_COLLAPSED -> behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            BottomSheetBehavior.STATE_HALF_EXPANDED -> behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun goToNextLowerState() {
        when (behavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            BottomSheetBehavior.STATE_HALF_EXPANDED -> behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

}