package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.familysns.R

class InviteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_invite, container, false)

        val inviteButton = view.findViewById<Button>(R.id.btn_invite)
        inviteButton.setOnClickListener {
            Toast.makeText(requireContext(), "초대 링크가 전송되었습니다!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}