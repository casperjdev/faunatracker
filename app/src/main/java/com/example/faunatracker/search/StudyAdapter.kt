package com.example.faunatracker.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.faunatracker.R
import com.example.faunatracker.map.MapActivity
import com.example.faunatracker.api.MovebankRepository
import com.example.faunatracker.databinding.ItemStudyBinding


class StudyAdapter(
    private var items: List<MovebankRepository.Study>
) : RecyclerView.Adapter<StudyAdapter.StudyViewHolder>() {

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
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<MovebankRepository.Study>) {
        items = newItems
        notifyDataSetChanged()
    }
}