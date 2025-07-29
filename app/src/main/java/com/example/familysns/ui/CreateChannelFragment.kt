package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateChannelFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_channel, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val editTitle = view.findViewById<EditText>(R.id.edit_channel_input_title)
        val editPassword = view.findViewById<EditText>(R.id.edit_channel_input_password)
        val editMotto = view.findViewById<EditText>(R.id.edit_channel_input_motto)
        
        // include된 버튼에서 찾기 (ImageView로 변경)
        val backButton = view.findViewById<ImageView>(R.id.btn_back)
        val nextButton = view.findViewById<ImageView>(R.id.btn_next)

        backButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        nextButton?.setOnClickListener {
            val title = editTitle.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val motto = editMotto.text.toString().trim()
            val currentUser = auth.currentUser

            if (title.isEmpty() || password.isEmpty() || motto.isEmpty()) {
                Toast.makeText(requireContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val familyData = hashMapOf(
                "name" to title,
                "password" to password,
                "motto" to motto,
                "members" to listOf(currentUser.uid)
            )

            firestore.collection("families")
                .add(familyData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "채널이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_createChannelFragment_to_inviteFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "채널 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}