package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R

class CreateChannelFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_channel, container, false)

        val backButton = view.findViewById<Button>(R.id.btn_back)
        val nextButton = view.findViewById<Button>(R.id.btn_next)

        backButton?.setOnClickListener {
            findNavController().popBackStack() // 뒤로가기
        }

        nextButton?.setOnClickListener {
            findNavController().navigate(R.id.action_createChannelFragment_to_inviteFragment)
        }

        return view
    }
}
