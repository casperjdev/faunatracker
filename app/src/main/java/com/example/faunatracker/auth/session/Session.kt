package com.example.faunatracker.auth.session

import androidx.lifecycle.MutableLiveData
import com.example.faunatracker.auth.api.AuthRepository
import com.example.faunatracker.auth.api.AuthRepository.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Session {
    val currentUser = MutableLiveData<User?>()
    val repo = AuthRepository()

    fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = repo.login(email, password)
                currentUser.postValue(user)
            } catch (e: Exception) {
                currentUser.postValue(null)
            }
        }
    }

    fun register(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val created = repo.register(email, password)
                currentUser.postValue(created)
            } catch (e: Exception) {
                currentUser.postValue(null)
            }
        }
    }
}