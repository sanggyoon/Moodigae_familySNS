package com.example.familysns.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.result.contract.ActivityResultContracts

class FamilyHomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                val imageUris = mutableListOf<String>()

                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        imageUris.add(uri.toString())
                    }
                } else {
                    result.data?.data?.let {
                        imageUris.add(it.toString())
                    }
                }

                // ✅ Firebase에서 familyId 가져오기
                val currentUserId = auth.currentUser?.uid ?: return@registerForActivityResult

                db.collection("users").document(currentUserId).get()
                    .addOnSuccessListener { doc ->
                        val familyIds = doc["familyId"] as? List<*>
                        val firstFamilyId = familyIds?.firstOrNull()?.toString()

                        if (!firstFamilyId.isNullOrEmpty()) {
                            val action = FamilyHomeFragmentDirections
                                .actionFamilyHomeFragmentToPostWriteFragment(
                                    familyId = firstFamilyId,
                                    imageUriList = imageUris.toTypedArray()
                                )
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), "가족 채널이 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_family_home, container, false)

        val sharePostButton = view.findViewById<Button>(R.id.btn_share_post)
        sharePostButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/* video/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickImageLauncher.launch(Intent.createChooser(intent, "미디어 선택"))
        }

        return view
    }
}