package com.example.faunatracker.api

import android.util.Log
import com.example.faunatracker.BuildConfig
import com.example.faunatracker.tools.Utils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest

class MovebankRepository {

    private val baseUrl = BuildConfig.MOVEBANK_URL
    private val token = BuildConfig.MOVEBANK_TOKEN
    private val api = ApiClient.create("")

    @OptIn(ExperimentalSerializationApi::class)
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

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class AnimalEvent(
        val timestamp: String,
        val location_lat: Double,
        val location_long: Double,
        val individual_local_identifier: String
    )

    suspend fun getStudies(): List<Study> {
        val csv = ApiClient.get(api, """
            $baseUrl
            ?entity_type=study
            &is_public=true
            &i_can_see_data=true
            &i_have_download_access=true
            &there_are_data_which_i_cannot_see=false
            &api-token=$token""".trimIndent())
        val json = Utils.csvToJson(csv.body.toString())

        val studies: List<Study> = Json.decodeFromString<List<Study>>(json)

        return studies
    }

    suspend fun getSingleStudy(id: String): Study {
        val csv = ApiClient.get(api, "$baseUrl?entity_type=study&study_id=$id&api-token=$token")
        val json = Utils.csvToJson(csv.body.toString())

        val study: List<Study> = Json.decodeFromString<List<Study>>(json)

        return study[0]
    }

    suspend fun getEventData(id: String): List<AnimalEvent> {
        val first = ApiClient.get(api, """
            https://www.movebank.org/movebank/service/direct-read
            ?entity_type=event
            &event_reduction_profile=EURING_01
            &study_id=$id
            &api-token=$token
        """.trimIndent())
        val body = first.body.orEmpty()

        Log.d("first-$id", body)

        // Case 1 — success immediately (CSV)
        if (!body.contains("License Terms:", ignoreCase = true)) {
            val json = Utils.csvToJson(body)
            val data = Json.decodeFromString<List<AnimalEvent>>(json)

            Log.d("first-$id-json", json)
            Log.d("first-$id-data", data.toString())

            return data
        }

        // Case 2 — license required → fetch license text
        val hash = md5(body)

        Log.d("hash-$id", hash)

        val retry = ApiClient.get(api, """
            https://www.movebank.org/movebank/service/direct-read
            ?entity_type=event
            &event_reduction_profile=EURING_01
            &study_id=$id
            &license-md5=$hash
            &api-token=$token
        """.trimIndent())

        val json = Utils.csvToJson(retry.body.toString())

        val data = Json.decodeFromString<List<AnimalEvent>>(json)

        Log.d("retry-$id-json", json)
        Log.d("retry-$id-data", data.toString())
        return data
    }

    // ---- MD5 ----
    private fun md5(text: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }


}