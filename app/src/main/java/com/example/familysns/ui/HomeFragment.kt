package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val createChannelButton = view.findViewById<Button>(R.id.btn_create_channel)
        val enterChannelButton = view.findViewById<Button>(R.id.btn_join_channel)

        createChannelButton?.setOnClickListener {
            findNavController().navigate(R.id.createChannelFragment)
        }

        enterChannelButton?.setOnClickListener {
            findNavController().navigate(R.id.enterChannelFragment) // 여기 추가됨
        }

        return view
    }
}