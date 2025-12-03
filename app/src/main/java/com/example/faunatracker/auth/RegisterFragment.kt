package com.example.faunatracker.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.faunatracker.R
import com.example.faunatracker.auth.session.Session
import com.example.faunatracker.tools.ToastHelper

class RegisterFragment : Fragment(R.layout.fragment_register) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEdit = view.findViewById<EditText>(R.id.register_email)
        val confirmEdit = view.findViewById<EditText>(R.id.register_confirm)
        val passwordEdit = view.findViewById<EditText>(R.id.register_password)
        val registerBtn = view.findViewById<Button>(R.id.button_register)
        val toLoginBtn = view.findViewById<TextView>(R.id.button_toLogin)

        toLoginBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        registerBtn.setOnClickListener {
            if (emailEdit.text.toString() == confirmEdit.text.toString()) {
                Session.register(emailEdit.text.toString(), passwordEdit.text.toString())
            }
        }

        Session.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                ToastHelper.show(context, "Registration failed (user might already exist)")
            }
            else Session.login(user.email, user.password)
        }
    }
}