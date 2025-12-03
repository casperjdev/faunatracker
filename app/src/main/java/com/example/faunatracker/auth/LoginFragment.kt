package com.example.faunatracker.auth

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.faunatracker.R
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.tools.ToastHelper

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEdit = view.findViewById<EditText>(R.id.login_email)
        val passwordEdit = view.findViewById<EditText>(R.id.login_password)
        val loginBtn = view.findViewById<Button>(R.id.button_login)
        val toRegisterBtn = view.findViewById<TextView>(R.id.button_toRegister)

        toRegisterBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        loginBtn.setOnClickListener {
            Session.login(emailEdit.text.toString(), passwordEdit.text.toString())
        }

        Session.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                ToastHelper.show(context, "Invalid username or password")
            }
        }
    }
}