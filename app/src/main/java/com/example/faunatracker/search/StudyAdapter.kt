package com.example.faunatracker.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.faunatracker.R
import com.example.faunatracker.map.MapActivity
import com.example.faunatracker.search.api.SearchRepository


class StudyAdapter(
    private var items: List<SearchRepository.Study>
) : RecyclerView.Adapter<StudyAdapter.StudyViewHolder>() {

    class StudyViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val name = item.findViewById<TextView>(R.id.studyName)
        val author = item.findViewById<TextView>(R.id.studyAuthor)
        val species = item.findViewById<TextView>(R.id.studySpecies)
        val animalCount = item.findViewById<TextView>(R.id.studyAnimalCount)
        val sensor = item.findViewById<TextView>(R.id.studySensor)
        val releaseDate = item.findViewById<TextView>(R.id.studyReleaseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study, parent, false)
        return StudyViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudyViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.author.text = if (item.principal_investigator_name.isEmpty()) "Unknown investigator" else item.principal_investigator_name
        holder.species.text = if (item.taxon_ids.isEmpty()) "No species provided" else item.taxon_ids.split(",")[0]
        holder.animalCount.text = if (item.number_of_individuals.isEmpty()) "Unknown number of animals" else item.number_of_individuals + " Animals"
        holder.sensor.text = if (item.sensor_type_ids.isEmpty()) "No sensor type provided" else item.sensor_type_ids
        holder.releaseDate.text = "Release Date: " + (if (item.go_public_date.isEmpty()) "Not provided" else item.go_public_date)

        holder.itemView.setOnClickListener { view ->
            val intent = Intent(view.context, MapActivity::class.java).apply {
                putExtra(MapActivity.EXTRA_STUDY_ID, item.id)
            }
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<SearchRepository.Study>) {
        items = newItems
        notifyDataSetChanged()
    }
}