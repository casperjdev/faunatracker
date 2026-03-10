package com.example.faunatracker.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.R
import com.example.faunatracker.api.SupabaseRepository.AuthResult.*
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.base.BaseFragment
import com.example.faunatracker.databinding.FragmentLoginBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        buttonToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonLogin.setOnClickListener {
            val uname = loginUname.text.toString()
            val password = loginPassword.text.toString()

            if (uname.isEmpty() || password.isEmpty()) {
                loginError.text = "Please enter credentials."
                return@setOnClickListener
            }

            loadingOverlay.visibility = View.VISIBLE
            loginError.text = ""

            lifecycleScope.launch {
                try {
                    val result = Session.login(uname, password)
                    loadingOverlay.visibility = View.GONE

                    when (result) {
                        is Success -> loginError.text = ""
                        is Error.InvalidInput -> loginError.text = "Please enter credentials."
                        is Error.InvalidCredentials -> loginError.text = "Invalid username or password."
                        is Error.NetworkError -> loginError.text = "A network error occurred. Try again later."
                        is Error.ServerError -> loginError.text = "A server error occurred. Try again later."
                        else -> loginError.text = "Something went wrong."
                    }
                } catch (_: Exception) {
                    loadingOverlay.visibility = View.GONE
                    loginError.text = "Something went wrong."
                }
            }
        }
    }
}