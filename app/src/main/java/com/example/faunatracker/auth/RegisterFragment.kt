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
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var unameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var confirmEdit: EditText
    private lateinit var registerBtn: MaterialButton
    private lateinit var toLoginBtn: TextView
    private lateinit var errorBoundary: TextView
    private lateinit var loadingSpinner: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unameEdit = view.findViewById(R.id.register_uname)
        passwordEdit = view.findViewById(R.id.register_password)
        confirmEdit = view.findViewById(R.id.register_confirm)
        registerBtn = view.findViewById(R.id.button_register)
        toLoginBtn = view.findViewById(R.id.button_toLogin)
        errorBoundary = view.findViewById(R.id.register_error)

        loadingSpinner = view.findViewById(R.id.loadingOverlay)

        toLoginBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        registerBtn.setOnClickListener {
            val uname = unameEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirm = confirmEdit.text.toString()

            if (uname.isEmpty() || confirm.isEmpty() || password.isEmpty()) {
                errorBoundary.text = "Please enter credentials."
                return@setOnClickListener
            } else if (password != confirm) {
                errorBoundary.text = "Password fields must match."
                return@setOnClickListener
            }

            loadingSpinner.visibility = View.VISIBLE
            errorBoundary.text = ""

            lifecycleScope.launch {
                try {
                    val result = Session.register(uname, password)
                    loadingSpinner.visibility = View.GONE

                    when (result) {
                        is Success -> errorBoundary.text = ""
                        is Error.InvalidInput -> errorBoundary.text = "Please enter credentials."
                        is Error.UserAlreadyExists -> errorBoundary.text = "User already exists."
                        is Error.NetworkError -> errorBoundary.text = "A network error occured. Try again later."
                        is Error.ServerError -> errorBoundary.text = "A server error occured. Try again later."
                        else -> errorBoundary.text = "Something went wrong."
                    }
                } catch (e: Exception) {
                    loadingSpinner.visibility = View.GONE
                    errorBoundary.text = "Something went wrong."
                }
            }
        }
    }
}