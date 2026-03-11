package com.example.faunatracker.map

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.faunatracker.R
import com.example.faunatracker.base.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.api.TaxonomyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import java.net.URL
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import org.maplibre.android.annotations.IconFactory
import com.example.faunatracker.api.SupabaseRepository
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.auth.session.Session.currentUser
import com.example.faunatracker.databinding.ActivityMapBinding
import com.example.faunatracker.search.SearchActivity

class MapActivity : BaseActivity<ActivityMapBinding>(ActivityMapBinding::inflate) {
    // SHEET
    private lateinit var behavior: BottomSheetBehavior<LinearLayout>
    private var hiddenHeight = 0
    private var halfHeight = 0
    private var lastSlideOffset = 0f
    private var slideDirection = SlideDirection.NONE
    enum class SlideDirection { UP, DOWN, NONE }

    // DATA
    companion object {
        const val EXTRA_STUDY_ID = "EXTRA_STUDY_ID"
    }
    private lateinit var studyId: String
    private lateinit var study: MovebankRepository.Study
    private val movebank = MovebankRepository()
    private val supabase = SupabaseRepository()

    private val prefs by lazy {
        getSharedPreferences("app_settings", MODE_PRIVATE)
    }

    override fun setInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val tappable = insets.getInsets(WindowInsetsCompat.Type.tappableElement())

            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0,0, bars.bottom)

            binding.navbar.updatePadding(top = status.top)
            binding.bottomSheet.updatePadding(bottom = tappable.bottom)

            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapLibre.getInstance(this@MapActivity)
        super.onCreate(savedInstanceState)

        initMap()
        initSheet()
        getStudyInfo()
        initTray()
    }

    private fun initMap() = with(binding) {
        mapView.getMapAsync { map ->
            val userTheme = prefs.getString("theme", "light")

            when (userTheme) {
                "Light" -> map.setStyle("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
                "Dark" -> map.setStyle("https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json")
                else -> map.setStyle("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
            }

            map.cameraPosition = CameraPosition.Builder().target(LatLng(0.0, 0.0)).zoom(1.0).build()
        }
    }

    // bottom sheet
    private fun initSheet() = with(binding) {
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.apply {
            isFitToContents = false
            skipCollapsed = false
            isDraggable = true
        }

        bottomSheet.post {
            setupBreakpoints()
        }
        attachCallbacks()
    }

    private fun setupBreakpoints() = with(binding) {
        hiddenHeight = thumb.height
        halfHeight = thumb.height + ( if (headerRow.isVisible) headerRow else contentLoading).height

        val parentHeight = (bottomSheet.parent as View).height

        val navbarHeight = navbar.height
        val sheetHeight = parentHeight - navbarHeight

        bottomSheet.post {
            val params = binding.bottomSheet.layoutParams
            params.height = sheetHeight
            binding.bottomSheet.layoutParams = params
        }

        behavior.apply {
            peekHeight = hiddenHeight
            halfExpandedRatio = halfHeight.toFloat() / parentHeight
            state = BottomSheetBehavior.STATE_HALF_EXPANDED
            expandedOffset = navbarHeight
        }
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

                val alpha = slideOffset.coerceIn(0f, 1f)

                binding.mapScrim.alpha = alpha
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

    // study
    private fun getStudyInfo() {
        studyId = intent.getStringExtra(EXTRA_STUDY_ID).toString()

        with(binding){
            studyId.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    mapLoading.visibility = View.VISIBLE

                    study = movebank.getSingleStudy( it)

                    withContext(Dispatchers.Main) {
                        study.let{ study ->
                            navbarText.text = study.name
                            navbarText.isSelected = true

                            val eventData = movebank.getEventData(study.id)

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

                                map.cameraPosition = CameraPosition.Builder().target(LatLng(eventData[0].location_lat, eventData[0].location_long)).zoom(10.0).build()
                            }

                            mapLoading.visibility = View.GONE
                            headerRow.visibility = View.GONE
                            contentLoading.visibility = View.VISIBLE

                            val species = if (!study.taxon_ids.isEmpty()) study.taxon_ids.split(",").getOrNull(0) else "Unknown Species"
                            if (species != null && species != "Unknown Species") {
                                val article = TaxonomyRepository().getArticle(species)
                                speciesName.apply {
                                    text = article?.title
                                    isSelected = true
                                }
                                speciesNameLatin.apply {
                                    text = species
                                    isSelected = true
                                }
                                wikiContent.text = article?.extract

                                article?.thumbnailUrl?.let { url ->
                                    val bitmap = withContext(Dispatchers.IO) {
                                        val connection = URL(url).openConnection()
                                        BitmapFactory.decodeStream(connection.getInputStream())
                                    }
                                    speciesImage.setImageBitmap(bitmap)
                                }

                                speciesCard.visibility = View.VISIBLE
                            } else {
                                speciesName.text = "Unknown Species"
                                wikiContent.text = "Could not fetch description."
                            }

                            contentLoading.visibility = View.GONE
                            headerRow.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    // tray events
    private fun initTray() = with(binding) {
        currentUser.observe(this@MapActivity) { user ->
            if (user != null) {
                val userStudies = user.saved_studies
                if (userStudies.contains(studyId)) {
                    btnSave.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.unpin)
                    btnSaveLabel.text = "Unpin"
                } else {
                    btnSave.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.pin)
                    btnSaveLabel.text = "Pin"
                }
            }
        }

        btnSave.setOnClickListener {
            val user = currentUser.value ?: return@setOnClickListener
            val newList = user.saved_studies.toMutableList()

            if (newList.contains(studyId)) {
                newList.remove(studyId)
            } else {
                newList.add(studyId)
            }

            val arrayString = newList.joinToString(separator = ",", prefix = "{", postfix = "}")

            val jsonBody = """
                {
                    "saved_studies": "$arrayString"
                }
            """.trimIndent()

            mapLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    supabase.updateUser(user.id, jsonBody)
                    mapLoading.visibility = View.GONE
                    Session.refresh()
                } catch (e: Exception) {
                    mapLoading.visibility = View.GONE
                    e.printStackTrace()
                }
            }
        }

        btnFindSimilar.setOnClickListener {
            val species = if (study.taxon_ids.isEmpty()) "" else study.taxon_ids.split(",")[0]

            val intent = Intent(this@MapActivity, SearchActivity::class.java).apply {
                putExtra(SearchActivity.EXTRA_QUERY, species)
            }
            startActivity(intent)
        }
    }


}