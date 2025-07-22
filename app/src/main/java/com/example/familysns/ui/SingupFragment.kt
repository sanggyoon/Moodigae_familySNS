package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.familysns.R

class SignupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

//        val emailEditText = view.findViewById<EditText>(R.id.editTextEmail)
//        val passwordEditText = view.findViewById<EditText>(R.id.editTextPassword)
//        val signupButton = view.findViewById<Button>(R.id.buttonSignup)

        return view
    }
}