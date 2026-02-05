package com.example.faunatracker.map

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap

import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.api.TaxonomyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import java.net.URL
import androidx.core.view.isVisible
import org.maplibre.android.annotations.IconFactory
import org.w3c.dom.Text

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
    private var study: MovebankRepository.Study? = null
    private lateinit var navbarTitle: TextView
    private lateinit var speciesImage: ImageView
    private lateinit var speciesName: TextView
    private lateinit var speciesLatin: TextView
    private lateinit var speciesContent: TextView

    // MAP
    private lateinit var mapView: MapView

    // LOADING UI
    private lateinit var mapLoading: FrameLayout
    private lateinit var contentLoading: FrameLayout

    private val prefs by lazy {
        getSharedPreferences("app_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MAP
        MapLibre.getInstance(this)

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.getMapAsync { map ->
            val userTheme = prefs.getString("theme", "light")

            when (userTheme) {
                "Light" -> map.setStyle("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
                "Dark" -> map.setStyle("https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json")
                else -> map.setStyle("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
            }

            map.cameraPosition = CameraPosition.Builder().target(LatLng(0.0, 0.0)).zoom(1.0).build()
        }

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
        navbarTitle.isSelected = true;
        speciesImage = findViewById(R.id.speciesImage)
        speciesName = findViewById(R.id.speciesName)
        speciesLatin = findViewById(R.id.speciesNameLatin)
        speciesName.isSelected = true;
        speciesLatin.isSelected = true;
        speciesContent = findViewById(R.id.wikiContent)

        // LOADING UI
        mapLoading = findViewById(R.id.mapLoading)
        contentLoading = findViewById(R.id.contentLoading)


        studyId?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                mapLoading.visibility = View.VISIBLE

                study = MovebankRepository().getSingleStudy(it)

                withContext(Dispatchers.Main) {
                    study?.let{ study ->
                        navbarTitle.text = study.name

                        val eventData = MovebankRepository().getEventData(study.id)

                        val drawable = ContextCompat.getDrawable(this@MapActivity, R.drawable.map_marker)
                        val bitmap = drawable?.toBitmap(width = 48, height = 48)
                        val icon =  IconFactory.getInstance(this@MapActivity)
                            .fromBitmap(bitmap!!)
                        mapView.getMapAsync { map ->
                            eventData.forEach { event ->
                                map.addMarker(MarkerOptions()
                                    .icon(icon)
                                    .position(LatLng(event.location_lat, event.location_long))
                                    .title("Individual ID: ${event.individual_local_identifier}")
                                    .snippet(event.timestamp))
                            }
                        }

                        mapLoading.visibility = View.GONE
                        headerRow.visibility = View.GONE
                        contentLoading.visibility = View.VISIBLE

                        val species = if (!study.taxon_ids.isEmpty()) study.taxon_ids.split(",").getOrNull(0) else "Unknown Species"
                        if (species != null && species != "Unknown Species") {
                            val article = TaxonomyRepository().getArticle(species)
                            speciesName.text = article?.title
                            speciesLatin.text = species
                            speciesContent.text = article?.extract

                            article?.thumbnailUrl?.let { url ->
                                val bitmap = withContext(Dispatchers.IO) {
                                    val connection = URL(url).openConnection()
                                    BitmapFactory.decodeStream(connection.getInputStream())
                                }
                                speciesImage.setImageBitmap(bitmap)
                            }
                        } else {
                            speciesName.text = "Unknown Species"
                            speciesContent.text = "Could not fetch description."
                            speciesImage.setImageResource(R.drawable.logo)
                            speciesImage.imageTintList = ColorStateList.valueOf(
                                when (prefs.getString("theme", "Light")) {
                                    "Light" -> ContextCompat.getColor(this@MapActivity, R.color.light_Primary)
                                    "Dark" -> ContextCompat.getColor(this@MapActivity, R.color.dark_Primary)
                                    else -> ContextCompat.getColor(this@MapActivity, R.color.light_Primary)
                                }
                            )
                        }

                        contentLoading.visibility = View.GONE
                        headerRow.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // HELPERS FOR SHEET
    private fun setupBreakpoints() {
        hiddenHeight = thumb.height
        halfHeight = thumb.height + ( if (headerRow.isVisible) headerRow else contentLoading).height

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