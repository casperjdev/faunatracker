package com.example.faunatracker.search.api

import com.example.faunatracker.BuildConfig
import com.example.faunatracker.api.ApiClient
import com.example.faunatracker.tools.Utils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

class SearchRepository {

    private val baseUrl = BuildConfig.MOVEBANK_URL

    private val token = BuildConfig.MOVEBANK_TOKEN

    private val api = ApiClient.create("")

    @Serializable
    @JsonIgnoreUnknownKeys
    data class Study (
        val id: String,
        val name: String,
        val principal_investigator_name: String,
        val taxon_ids: String,
        val sensor_type_ids: String,
        val go_public_date: String,
        val number_of_individuals: String

    )

    suspend fun getStudies(): List<Study> {
        val csv = ApiClient.get(api, "$baseUrl?entity_type=study&i_can_see_data=true&api-token=$token")
        val json = Utils.csvToJson(csv)

        val studies: List<Study> = Json.decodeFromString<List<Study>>(json)

        return studies
    }

    suspend fun getSingleStudy(id: String): Study {
        val csv = ApiClient.get(api, "$baseUrl?entity_type=study&study_id=$id&api-token=$token")
        val json = Utils.csvToJson(csv)

        val study: List<Study> = Json.decodeFromString<List<Study>>(json)

        return study[0]
    }


}