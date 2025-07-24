package com.example.familysns.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.familysns.R
import com.example.familysns.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.activity.result.contract.ActivityResultContracts

class FamilyHomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var todayPostsContainer: LinearLayout

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
        todayPostsContainer = view.findViewById(R.id.today_posts_container)

        val sharePostButton = view.findViewById<Button>(R.id.btn_share_post)
        sharePostButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/* video/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickImageLauncher.launch(Intent.createChooser(intent, "미디어 선택"))
        }

        loadTodayPosts()
        return view
    }

    private fun loadTodayPosts() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val familyIds = doc["familyId"] as? List<*>
                val familyId = familyIds?.firstOrNull()?.toString() ?: return@addOnSuccessListener

                db.collection("families").document(familyId).collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener { result ->
                        todayPostsContainer.removeAllViews()
                        for (document in result) {
                            val post = document.toObject(Post::class.java)
                            val card = layoutInflater.inflate(R.layout.item_post_card, todayPostsContainer, false)
                            val imageView = card.findViewById<ImageView>(R.id.iv_thumbnail)
                            val textView = card.findViewById<TextView>(R.id.tv_message)
                            textView.text = post.message
                            Glide.with(this).load(post.imageUrls.firstOrNull()).into(imageView)
                            todayPostsContainer.addView(card)
                        }
                    }
            }
    }
}