package com.example.familysns.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        // SharedPreferences에서 familyId 가져오기
        val familyId = PrefsHelper.getFamilyId(requireContext())
        
        // 채널 코드 클릭 시 복사 기능
        val channelCodeDisplay = view.findViewById<TextView>(R.id.tv_channel_id)
        channelCodeDisplay?.text = familyId ?: "N/A"
        channelCodeDisplay?.setOnClickListener {
            copyToClipboard(channelCodeDisplay.text.toString())
        }

        // 버튼 기능 추가 (include된 레이아웃에서 버튼 찾기)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val btnNext = view.findViewById<ImageView>(R.id.btn_next)

        btnBack?.setOnClickListener {
            findNavController().popBackStack()
        }

        btnNext?.setOnClickListener {
            findNavController().navigate(R.id.action_inviteFragment_to_familyHomeFragment)
        }

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("채널 코드", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireContext(), "채널 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
    }
}