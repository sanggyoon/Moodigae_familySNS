package com.example.familysns.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.familysns.R
import com.example.familysns.model.Post
import com.example.familysns.util.PrefsHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.activity.result.contract.ActivityResultContracts

class FamilyHomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var todayPostsContainer: LinearLayout
    private lateinit var textFamilyName: TextView
    private lateinit var textMotto: TextView

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

                val familyId = PrefsHelper.getFamilyId(requireContext())
                if (!familyId.isNullOrEmpty()) {
                    val action = FamilyHomeFragmentDirections
                        .actionFamilyHomeFragmentToPostWriteFragment(
                            familyId = familyId,
                            imageUriList = imageUris.toTypedArray()
                        )
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(requireContext(), "가족 채널이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_family_home, container, false)

        todayPostsContainer = view.findViewById(R.id.today_posts_container)
        textFamilyName = view.findViewById(R.id.text_family_name)
        textMotto = view.findViewById(R.id.text_motto)

        view.findViewById<Button>(R.id.btn_share_post).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/* video/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickImageLauncher.launch(Intent.createChooser(intent, "미디어 선택"))
        }

        loadFamilyInfo()
        loadTodayPosts()
        return view
    }

    private fun loadFamilyInfo() {
        val familyId = PrefsHelper.getFamilyId(requireContext()) ?: return

        db.collection("families").document(familyId).get()
            .addOnSuccessListener { document ->
                val familyName = document.getString("name") ?: "우리 가족"
                val motto = document.getString("motto") ?: "가훈이 없습니다."

                textFamilyName.text = familyName
                textMotto.text = motto
            }
            .addOnFailureListener {
                textFamilyName.text = "가족 이름 불러오기 실패"
                textMotto.text = "가훈 불러오기 실패"
            }
    }

    private fun loadTodayPosts() {
        val familyId = PrefsHelper.getFamilyId(requireContext()) ?: return

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