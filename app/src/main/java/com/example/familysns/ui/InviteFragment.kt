package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R
import com.example.familysns.util.PrefsHelper

class InviteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_invite, container, false)

        val tvChannelId = view.findViewById<TextView>(R.id.tv_channel_id)
        val btnNext = view.findViewById<Button>(R.id.btn_next)
        val btnBack = view.findViewById<Button>(R.id.btn_back)

        // SharedPreferences에서 familyId 가져오기
        val familyId = PrefsHelper.getFamilyId(requireContext())
        tvChannelId.text = familyId ?: "N/A"

        // '다음' → FamilyHomeFragment 이동
        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_inviteFragment_to_familyHomeFragment)
        }

        // '뒤로' → 이전 화면으로 돌아가기
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }
}