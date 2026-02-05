package com.example.faunatracker.auth.session

import androidx.lifecycle.MutableLiveData
import com.example.faunatracker.api.SupabaseRepository
import com.example.faunatracker.api.SupabaseRepository.*

object Session {
    val currentUser = MutableLiveData<User?>()
    val repo = SupabaseRepository()

    suspend fun login(uname: String, password: String): AuthResult {
        val result = repo.login(uname, password)
        if (result is AuthResult.Success) {
            currentUser.postValue(result.user)
        }
        return result
    }

    suspend fun register(uname: String, password: String): AuthResult {
        val result = repo.register(uname, password)
        if (result is AuthResult.Success) {
            currentUser.postValue(result.user)
        }
        return result
    }

    fun logout() {
        currentUser.postValue(null)
    }

    suspend fun refresh() {
        if (currentUser.value != null) {
            val latest = repo.getUser(currentUser.value!!.id)
            currentUser.postValue(latest)
        }
    }

}