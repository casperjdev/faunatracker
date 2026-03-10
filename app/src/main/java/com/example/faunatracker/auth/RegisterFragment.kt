package com.example.faunatracker.auth

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.R
import com.example.faunatracker.api.SupabaseRepository.AuthResult.*
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.base.BaseFragment
import com.example.faunatracker.databinding.FragmentRegisterBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RegisterFragment : BaseFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        buttonToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonRegister.setOnClickListener {
            val uname = registerUname.text.toString()
            val password = registerPassword.text.toString()
            val confirm = registerConfirm.text.toString()

            if (uname.isEmpty() || confirm.isEmpty() || password.isEmpty()) {
                registerError.text = "Please enter credentials."
                return@setOnClickListener
            } else if (password != confirm) {
                registerError.text = "Password fields must match."
                return@setOnClickListener
            }

            loadingOverlay.visibility = View.VISIBLE
            registerError.text = ""

            lifecycleScope.launch {
                try {
                    val result = Session.register(uname, password)
                    loadingOverlay.visibility = View.GONE

                    when (result) {
                        is Success -> registerError.text = ""
                        is Error.InvalidInput -> registerError.text = "Please enter credentials."
                        is Error.UserAlreadyExists -> registerError.text = "User already exists."
                        is Error.NetworkError -> registerError.text = "A network error occured. Try again later."
                        is Error.ServerError -> registerError.text = "A server error occured. Try again later."
                        else -> registerError.text = "Something went wrong."
                    }
                } catch (e: Exception) {
                    loadingOverlay.visibility = View.GONE
                    registerError.text = "Something went wrong."
                }
            }
        }
    }
}