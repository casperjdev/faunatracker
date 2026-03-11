package com.example.faunatracker.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.R
import com.example.faunatracker.map.MapActivity
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.api.SupabaseRepository
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.auth.session.Session.currentUser
import com.example.faunatracker.databinding.ContextMenuBinding
import com.example.faunatracker.databinding.ItemStudyBinding
import com.example.faunatracker.search.SearchActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch


class StudyAdapter(
    private var items: List<MovebankRepository.Study>
) : RecyclerView.Adapter<StudyAdapter.StudyViewHolder>() {

    private val repo = SupabaseRepository()

    class StudyViewHolder(
        val binding: ItemStudyBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        val binding = ItemStudyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        val item = items[position]

        with(holder.binding) {
            studyName.text = item.name
            studyAuthor.text = if (item.principal_investigator_name.isEmpty()) "Unknown investigator" else item.principal_investigator_name
            studySpecies.text = if (item.taxon_ids.isEmpty()) "No species provided" else item.taxon_ids.split(",")[0]
            studyAnimalCount.text = if (item.number_of_individuals.isEmpty()) "Unknown number of animals" else item.number_of_individuals + " Animals"
            studySensor.text = if (item.sensor_type_ids.isEmpty()) "No sensor type provided" else item.sensor_type_ids
            studyReleaseDate.text = "Release Date: " + (if (item.go_public_date.isEmpty()) "Not provided" else item.go_public_date)
        }

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, MapActivity::class.java).apply {
                putExtra(MapActivity.EXTRA_STUDY_ID, item.id)
            }
            view.context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener { view ->
            showContextMenu(item, view)
            true
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<MovebankRepository.Study>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun showContextMenu(study: MovebankRepository.Study, view: View) {
        val binding = ContextMenuBinding.inflate(LayoutInflater.from(view.context))
        val dialog = Dialog(view.context)

        dialog.setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            dialog.dismiss()
            val user = currentUser.value ?: return@setOnClickListener
            val newList = user.saved_studies.toMutableList()
            newList.remove(study.id)

            val arrayString = newList.joinToString(separator = ",", prefix = "{", postfix = "}")
            val jsonBody = """
                {
                    "saved_studies": "$arrayString"
                }
            """.trimIndent()

            (view.context as? AppCompatActivity)?.lifecycleScope?.launch {
                try {
                    repo.updateUser(user.id, jsonBody)
                    Session.refresh()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.btnFindSimilar.setOnClickListener {
            dialog.dismiss()
            val species = if (study.taxon_ids.isEmpty()) "No species provided" else study.taxon_ids.split(",")[0]

            val intent = Intent(view.context, SearchActivity::class.java).apply {
                putExtra(SearchActivity.EXTRA_QUERY, species)
            }
            view.context.startActivity(intent)
        }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.START or Gravity.CENTER_VERTICAL)
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        dialog.show()
    }
}