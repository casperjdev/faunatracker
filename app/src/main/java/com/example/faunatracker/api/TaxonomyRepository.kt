package com.example.faunatracker.api

import android.R
import android.util.Log
import com.example.faunatracker.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject
import java.util.Locale
import java.util.Locale.getDefault

class TaxonomyRepository {
    // https://api.gbif.org/v1/species/match?name=canis_lupus&strict=true
    // https://api.gbif.org/v1/species/5219173
    // https://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles=gray_wolf&explaintext=1&format=json

    private val gbifUrl = BuildConfig.GBIF_URL
    private val wikipediaUrl = BuildConfig.WIKIPEDIA_URL
    private val api = ApiClient.create("")

    data class WikiArticle(
        val title: String,
        val extract: String,
        val thumbnailUrl: String
    )

    suspend fun getUsageKey(species: String): Int {
        val speciesSlug = species.lowercase(getDefault()).replace(" ", "_")
        val res = ApiClient.get(api, "$gbifUrl/species/match?name=$speciesSlug&strict=true")

        val json = JSONObject(res.body?: "")
        val key = json.getInt("usageKey")
        return key


    }

    suspend fun getVernacularName(key: Int): String {
        val res = ApiClient.get(api, "$gbifUrl/species/$key")
        val json = JSONObject(res.body?: "")
        val name = json.getString("vernacularName")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            .replace(" ", "_")
        return name
    }

    suspend fun getArticle(species: String): WikiArticle? {
        val key = getUsageKey(species)
        val vernacular = getVernacularName(key)

        // 1. Try vernacular name
        fetchArticleByTitle(vernacular)?.let { return it }

        // 2. Search fallback using vernacular
        searchWikipedia(vernacular)?.let { title ->
            fetchArticleByTitle(title)?.let { return it }
        }

        // 3. Try Latin name
        fetchArticleByTitle(species.replace(" ", "_"))?.let { return it }

        // 4. Search fallback using Latin name
        searchWikipedia(species)?.let { title ->
            fetchArticleByTitle(title)?.let { return it }
        }

        return null
    }

    suspend fun fetchArticleByTitle(title: String): WikiArticle? {
        val res = ApiClient.get(
            api,
            "$wikipediaUrl?action=query&prop=extracts|pageimages&titles=$title&explaintext=1&pithumbsize=600&format=json",
            mapOf("User-Agent" to "FaunaTracker/0.0 (casperj.dev@gmail.com) okhttp/3.0")
        )

        val json = JSONObject(res.body ?: "")
        val pages = json.getJSONObject("query").getJSONObject("pages")
        val firstKey = pages.keys().next()
        val page = pages.getJSONObject(firstKey)

        if (!page.has("extract")) return null

        val extract = page.getString("extract")
        if (extract.isBlank()) return null

        val resolvedTitle = page.getString("title")

        val thumbnail = page.optJSONObject("thumbnail")
            ?.optString("source")

        return WikiArticle(
            title = resolvedTitle,
            extract = extract,
            thumbnailUrl = thumbnail?: ""
        )
    }

    suspend fun searchWikipedia(query: String): String? {
        val res = ApiClient.get(
            api,
            "$wikipediaUrl?action=query&list=search&srsearch=$query&format=json",
            mapOf("User-Agent" to "FaunaTracker/0.0 (casperj.dev@gmail.com) okhttp/3.0")
        )

        val json = JSONObject(res.body ?: "")
        val search = json.getJSONObject("query").getJSONArray("search")

        if (search.length() == 0) return null

        return search.getJSONObject(0).getString("title")
    }
}