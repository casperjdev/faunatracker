package com.example.faunatracker.api

import com.example.faunatracker.BuildConfig
import com.example.faunatracker.auth.session.Session
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.security.MessageDigest

class SupabaseRepository {

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val apiKey = BuildConfig.SUPABASE_API_KEY
    private val api = ApiClient.create(apiKey)

    @Serializable
    data class User (
        val id: Int,
        val uname: String,
        val password: String,
        val saved_studies: List<String>,
        val created_at: String
    )

    sealed class AuthResult {
        data class Success(val user: User) : AuthResult()

        sealed class Error : AuthResult() {
            object UserAlreadyExists : Error()
            object InvalidCredentials : Error()
            object InvalidInput : Error()
            object NetworkError : Error()
            object ServerError : Error()
            data class Unknown(val cause: Throwable?) : Error()
        }
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    suspend fun register(uname: String, password: String): AuthResult {
        val hashed = hashPassword(password)

        val jsonBody = """
            {
                "uname": "$uname",
                "password": "$hashed"
            }
        """.trimIndent()

        val result = try {
            ApiClient.post(api, "$baseUrl/users", jsonBody)
        } catch (e: IOException) {
            return AuthResult.Error.NetworkError
        }

        when (result.statusCode) {
            201, 200 -> {
                val req = ApiClient.get(api, "$baseUrl/users?uname=eq.$uname")
                val users: List<User> = Json.Default.decodeFromString(req.body.toString())
                return users.firstOrNull()
                    ?.let { AuthResult.Success(it) }
                    ?: AuthResult.Error.ServerError
            }

            409 -> return AuthResult.Error.UserAlreadyExists
            400 -> return AuthResult.Error.InvalidInput
            401 -> return AuthResult.Error.InvalidCredentials
            in 500..599 -> return AuthResult.Error.ServerError

            else -> return AuthResult.Error.Unknown(null)
        }
    }

    suspend fun login(uname: String, password: String): AuthResult {
        val result = try {
            ApiClient.get(api, "$baseUrl/users?uname=eq.$uname")
        } catch (e: IOException) {
            return AuthResult.Error.NetworkError
        }

        when (result.statusCode) {
            200 -> {
                val users: List<User> =
                    Json.Default.decodeFromString(result.body ?: "[]")

                return if (users.firstOrNull()?.password == hashPassword(password)) {
                    users.firstOrNull()
                        ?.let { AuthResult.Success(it) }
                        ?: AuthResult.Error.InvalidCredentials
                } else {
                    AuthResult.Error.InvalidCredentials
                }
            }

            401 -> return AuthResult.Error.InvalidCredentials
            403 -> return AuthResult.Error.InvalidCredentials
            in 500..599 -> return AuthResult.Error.ServerError
            else -> return AuthResult.Error.Unknown(null)
        }
    }

    suspend fun getUser(id: Int): User {
        val result = ApiClient.get(api, "$baseUrl/users?id=eq.$id")
        when (result.statusCode) {
            200 -> {
                val user: List<User> = Json.decodeFromString(result.body.toString())
                return user[0]
            }
            else -> return Session.currentUser.value!!
        }
    }

    suspend fun updateUser(id: Int, body: String): Boolean {
        val result = ApiClient.patch(api, "$baseUrl/users?id=eq.$id", body)

        return when (result.statusCode) {
            in 200..204 -> {
                true
            }
            else -> false;
        }
    }
}