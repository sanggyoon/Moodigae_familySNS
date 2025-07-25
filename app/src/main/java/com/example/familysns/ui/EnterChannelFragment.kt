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

class EnterChannelFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_enter_channel, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameEditText = view.findViewById<EditText>(R.id.edit_channel_name)
        val passwordEditText = view.findViewById<EditText>(R.id.edit_channel_password)
        val joinButton = view.findViewById<Button>(R.id.btn_join_channel)

        joinButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val userId = auth.currentUser?.uid

            if (name.isEmpty() || password.isEmpty() || userId == null) {
                Toast.makeText(requireContext(), "정보를 모두 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("families")
                .whereEqualTo("name", name)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents[0]
                        val familyId = doc.id

                        // Prefs에 저장
                        PrefsHelper.saveFamilyId(requireContext(), familyId)

                        // 사용자 문서에 familyId 추가
                        db.collection("users").document(userId)
                            .update("familyId", com.google.firebase.firestore.FieldValue.arrayUnion(familyId))
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "채널에 입장했습니다", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_enterChannelFragment_to_familyHomeFragment)
                            }
                    } else {
                        Toast.makeText(requireContext(), "채널 정보가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        return view
    }
}