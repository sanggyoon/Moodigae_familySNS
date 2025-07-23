package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R

class FamilyHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_family_home, container, false)

        val sharePostButton = view.findViewById<Button>(R.id.btn_share_post)
        sharePostButton.setOnClickListener {
            findNavController().navigate(R.id.action_familyHomeFragment_to_postCreateFragment)
        }

        return view
    }
}