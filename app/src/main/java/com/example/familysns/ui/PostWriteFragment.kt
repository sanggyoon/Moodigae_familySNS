package com.example.familysns.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.familysns.databinding.FragmentPostWriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController
import com.example.familysns.R

class PostWriteFragment : Fragment() {
    private lateinit var binding: FragmentPostWriteBinding
    private val args: PostWriteFragmentArgs by navArgs()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val selectedMembers = mutableSetOf<String>()
    private var selectedTag: String? = null
    private val tagOptions = listOf("여행", "생일", "외식")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImagePreview()
        loadFamilyMembers()
        setupAlbumTags()
        setupNextButton()
    }

    private fun getFamilyId(callback: (String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(null)
        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val familyIds = userDoc["familyId"] as? List<*>
                callback(familyIds?.firstOrNull()?.toString())
            }
            .addOnFailureListener { callback(null) }
    }

    private fun setupImagePreview() {
        val container = binding.imagePreviewContainer
        args.imageUriList?.forEach { uriString ->
            val uri = Uri.parse(uriString)
            val imageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(300, 300)
                setImageURI(uri)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(8, 0, 8, 0)
            }
            container.addView(imageView)
        }
    }

    private fun loadFamilyMembers() {
        getFamilyId { familyId ->
            if (familyId == null) return@getFamilyId
            val memberContainer = binding.familyTagContainer
            db.collection("families").document(familyId).get().addOnSuccessListener { doc ->
                val memberIds = doc["members"] as? List<*> ?: return@addOnSuccessListener
                memberIds.forEach { memberId ->
                    val id = memberId.toString()
                    db.collection("users").document(id).get().addOnSuccessListener { userDoc ->
                        val name = userDoc["name"] as? String ?: return@addOnSuccessListener
                        val btn = MaterialButton(requireContext()).apply {
                            text = name
                            isCheckable = true
                            isChecked = selectedMembers.contains(id)
                            setBackgroundColor(if (isChecked) 0xFFE0F7FA.toInt() else 0xFFFFFFFF.toInt())
                            setTextColor(if (isChecked) 0xFF00796B.toInt() else 0xFF000000.toInt())

                            setOnClickListener {
                                if (selectedMembers.contains(id)) {
                                    selectedMembers.remove(id)
                                    isChecked = false
                                    setBackgroundColor(0xFFFFFFFF.toInt()) // 흰 배경
                                    setTextColor(0xFF000000.toInt())      // 검정 텍스트
                                } else {
                                    selectedMembers.add(id)
                                    isChecked = true
                                    setBackgroundColor(0xFFE0F7FA.toInt()) // 연한 청록 배경
                                    setTextColor(0xFF00796B.toInt())       // 짙은 청록 텍스트
                                }
                            }
                        }
                        memberContainer.addView(btn)
                    }
                }
            }
        }
    }

    private fun setupAlbumTags() {
        val tagContainer = binding.albumTagContainer
        tagOptions.forEach { tag ->
            val btn = MaterialButton(requireContext()).apply {
                text = tag
                isCheckable = true
                setBackgroundColor(0xFFFFFFFF.toInt())
                setTextColor(0xFF000000.toInt())

                setOnClickListener {
                    selectedTag = if (selectedTag == tag) {
                        isChecked = false
                        updateTagButtons(tagContainer, "")
                        null
                    } else {
                        updateTagButtons(tagContainer, tag)
                        tag
                    }
                }
            }
            tagContainer.addView(btn)
        }
    }

    private fun updateTagButtons(container: FlexboxLayout, selected: String) {
        for (i in 0 until container.childCount) {
            val btn = container.getChildAt(i) as? MaterialButton ?: continue
            if (btn.text == selected) {
                btn.isChecked = true
                btn.setBackgroundColor(0xFFFFF9C4.toInt()) // 연한 노랑
                btn.setTextColor(0xFFF57F17.toInt())       // 진한 주황
            } else {
                btn.isChecked = false
                btn.setBackgroundColor(0xFFFFFFFF.toInt()) // 기본 흰색
                btn.setTextColor(0xFF000000.toInt())       // 기본 검정
            }
        }
    }

    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            val message = binding.editText.text.toString()
            val imageUris = args.imageUriList?.toList() ?: emptyList()
            if (imageUris.isEmpty()) {
                Toast.makeText(requireContext(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 로딩 시작
            binding.btnNext.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            uploadImagesToStorage(imageUris)
        }
    }

    private fun uploadImagesToStorage(uriStrings: List<String>) {
        getFamilyId { familyId ->
            if (familyId == null) {
                Toast.makeText(context, "가족 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@getFamilyId
            }

            val uploadedUrls = mutableListOf<String>()
            uriStrings.forEachIndexed { index, uriString ->
                val uri = Uri.parse(uriString)
                val ext = if (uriString.endsWith(".mp4")) "mp4" else "jpg"
                val fileName = "post_${System.currentTimeMillis()}_${index}.$ext"
                val fileRef = storage.reference.child("posts/$familyId/$fileName")

                fileRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    fileRef.downloadUrl
                }.addOnSuccessListener { downloadUrl ->
                    uploadedUrls.add(downloadUrl.toString())
                    if (uploadedUrls.size == uriStrings.size) {
                        savePostToFirestore(familyId, uploadedUrls)
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "파일 업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePostToFirestore(familyId: String, uploadedUrls: List<String>) {
        val message = binding.editText.text.toString()
        val authorId = auth.currentUser?.uid ?: "unknown"

        val post = hashMapOf(
            "authorId" to authorId,
            "message" to message,
            "imageUrls" to uploadedUrls,
            "participants" to selectedMembers.toList(),
            "tag" to selectedTag,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("families")
            .document(familyId)
            .collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(context, "게시물이 업로드되었습니다.", Toast.LENGTH_SHORT).show()

                // ✅ 로딩 종료
                binding.progressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true

                findNavController().navigate(R.id.action_postWriteFragment_to_familyHomeFragment)
            }
            .addOnFailureListener {
                Toast.makeText(context, "업로드 실패: ${it.message}", Toast.LENGTH_SHORT).show()

                // ✅ 로딩 종료
                binding.progressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true
            }
    }
}