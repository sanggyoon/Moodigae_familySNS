package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R
import com.example.familysns.util.PrefsHelper
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
        val backButton = view.findViewById<Button>(R.id.btn_back)
        val nextButton = view.findViewById<Button>(R.id.btn_next)

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
                .addOnSuccessListener { documentRef ->
                    val familyId = documentRef.id
                    val userId = auth.currentUser?.uid ?: return@addOnSuccessListener

                    // SharedPreferences에 저장
                    PrefsHelper.saveFamilyId(requireContext(), familyId)

                    // 🔥 users 컬렉션의 현재 사용자 문서에 familyId 배열 추가
                    firestore.collection("users").document(userId)
                        .update("familyId", com.google.firebase.firestore.FieldValue.arrayUnion(familyId))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "채널이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_createChannelFragment_to_inviteFragment)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "사용자 정보 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "채널 생성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}