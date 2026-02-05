package com.example.faunatracker.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.faunatracker.R
import com.example.faunatracker.api.SupabaseRepository.AuthResult.*
import com.example.faunatracker.auth.session.Session
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var unameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginBtn: MaterialButton
    private lateinit var toRegisterBtn: TextView
    private lateinit var errorBoundary: TextView
    private lateinit var loadingSpinner: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unameEdit = view.findViewById(R.id.login_uname)
        passwordEdit = view.findViewById(R.id.login_password)
        loginBtn = view.findViewById(R.id.button_login)
        toRegisterBtn = view.findViewById(R.id.button_toRegister)
        errorBoundary = view.findViewById(R.id.login_error)

        loadingSpinner = view.findViewById(R.id.loadingOverlay)

        toRegisterBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        loginBtn.setOnClickListener {
            val uname = unameEdit.text.toString()
            val password = passwordEdit.text.toString()

            if (uname.isEmpty() || password.isEmpty()) {
                errorBoundary.text = "Please enter credentials."
                return@setOnClickListener
            }

            loadingSpinner.visibility = View.VISIBLE
            errorBoundary.text = ""

            lifecycleScope.launch {
                try {
                    val result = Session.login(uname, password)
                    loadingSpinner.visibility = View.GONE

                    when (result) {
                        is Success -> errorBoundary.text = ""
                        is Error.InvalidInput -> errorBoundary.text = "Please enter credentials."
                        is Error.InvalidCredentials -> errorBoundary.text = "Invalid username or password."
                        is Error.NetworkError -> errorBoundary.text = "A network error occurred. Try again later."
                        is Error.ServerError -> errorBoundary.text = "A server error occurred. Try again later."
                        else -> errorBoundary.text = "Something went wrong."
                    }
                } catch (_: Exception) {
                    loadingSpinner.visibility = View.GONE
                    errorBoundary.text = "Something went wrong."
                }
            }
        }
    }
}