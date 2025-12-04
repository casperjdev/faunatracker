package com.example.faunatracker.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(apiKey: String): OkHttpClient {
        val interceptor = Interceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val request: Request = original.newBuilder()
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .build()
    }

    suspend fun get(client: OkHttpClient, url: String, headers: Map<String, String> = emptyMap()): String {
        val deferred = CoroutineScope(Dispatchers.IO).async {
            val requestBuilder = Request.Builder().url(url)
            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw RuntimeException("Unexpected code $response")
                return@async response.body.string()
            }
        }

        return deferred.await()
    }

    suspend fun post(client: OkHttpClient, url: String, body: String, headers: Map<String, String> = emptyMap()): String {
        val deferred = CoroutineScope(Dispatchers.IO).async {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = body.toRequestBody(mediaType)

            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)

            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw RuntimeException("Unexpected code $response")
                return@async response.body.string()
            }
        }

        return deferred.await()
    }
}