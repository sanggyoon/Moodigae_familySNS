package com.example.familysns.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        // 채널 코드 클릭 시 복사 기능
        val channelCodeDisplay = view.findViewById<TextView>(R.id.channel_code_display)
        channelCodeDisplay.setOnClickListener {
            copyToClipboard(channelCodeDisplay.text.toString())
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