package com.example.faunatracker.auth.api

import com.example.faunatracker.BuildConfig
import com.example.faunatracker.api.ApiClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AuthRepository {

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val apiKey = BuildConfig.SUPABASE_API_KEY

    private val api = ApiClient.create(apiKey)

    @Serializable
    data class User (
        val id: Int,
        val email: String,
        val password: String,
        val created_at: String
    )

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    suspend fun register(email: String, password: String): User? {
        val hashed = hashPassword(password)

        val jsonBody = """
            {
                "email": "$email",
                "password": "$hashed"
            }
        """.trimIndent()

        ApiClient.post(api, "$baseUrl/users", jsonBody)

        val req = ApiClient.get(api, "$baseUrl/users?email=eq.$email")

        val users: List<User> = Json.decodeFromString(req)
        if (users.isEmpty()) return null
        return users[0]
    }

    suspend fun login(email: String, password: String): User? {
        val req = ApiClient.get(api, "$baseUrl/users?email=eq.$email")
        val users: List<User> = Json.decodeFromString(req)
        if (users.isEmpty()) return null
        if (users[0].password == hashPassword(password)) {
            return users[0]
        }
        return null
    }
}