package com.example.faunatracker.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    data class HttpResult(
        val statusCode: Int,
        val body: String?
    )

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(apiKey: String): OkHttpClient {
        val interceptor = Interceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val builder: Request.Builder = original.newBuilder()

            if (apiKey.isNotBlank()) {
                builder.addHeader("apikey", apiKey)
                builder.addHeader("Authorization", "Bearer $apiKey")
            }

            chain.proceed(builder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .build()
    }

    suspend fun get(
        client: OkHttpClient,
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResult = withContext(Dispatchers.IO) {

        val requestBuilder = Request.Builder().url(url)
        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

        client.newCall(requestBuilder.build()).execute().use { response ->
            HttpResult(
                statusCode = response.code,
                body = response.body.string()
            )
        }
    }

    suspend fun post(
        client: OkHttpClient,
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResult = withContext(Dispatchers.IO) {

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = body.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

        client.newCall(requestBuilder.build()).execute().use { response ->
            HttpResult(
                statusCode = response.code,
                body = response.body.string()
            )
        }
    }

    suspend fun patch(
        client: OkHttpClient,
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResult = withContext(Dispatchers.IO) {

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = body.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(url)
            .patch(requestBody)

        headers.forEach { (k, v) -> requestBuilder.addHeader(k, v) }

        client.newCall(requestBuilder.build()).execute().use { response ->
            HttpResult(
                statusCode = response.code,
                body = response.body.string()
            )
        }
    }

}