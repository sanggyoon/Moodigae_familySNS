package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.familysns.R
import com.example.familysns.adapter.ImageSliderAdapter
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class PostDetailFragment : Fragment() {

    private lateinit var imageSlider: ViewPager2
    private lateinit var textUsername: TextView
    private lateinit var textMessage: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var textTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)

        // 🔹 XML에서 정의된 뷰 연결
        imageSlider = view.findViewById(R.id.image_slider)
        textUsername = view.findViewById(R.id.tv_username)
        textMessage = view.findViewById(R.id.tv_message)
        ivProfile = view.findViewById(R.id.iv_profile)
        textTime = view.findViewById(R.id.tv_time)

        // 🔹 Safe Args or Bundle에서 postId와 familyId 전달 받음
        val args = PostDetailFragmentArgs.fromBundle(requireArguments())
        val postId = args.postId
        val familyId = args.familyId

        // 🔹 Firestore 인스턴스
        val db = FirebaseFirestore.getInstance()

        // 🔹 해당 post 문서 가져오기
        db.collection("families")
            .document(familyId)
            .collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { doc ->
                val message = doc.getString("message") ?: ""
                val authorId = doc.getString("authorId") ?: ""
                val createdAt = doc.getTimestamp("createdAt")?.toDate()
                val imageUrls = doc.get("imageUrls") as? List<String> ?: emptyList()

                textMessage.text = message
                textTime.text = convertTimestampToTimeAgo(createdAt?.time ?: 0)

                // 🔹 이미지 슬라이더 연결
                if (imageUrls.isNotEmpty()) {
                    val adapter = ImageSliderAdapter(imageUrls)
                    imageSlider.adapter = adapter
                }

                // 🔹 작성자 이름 및 프로필 사진 불러오기
                db.collection("users").document(authorId)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "작성자"
                        val profileUrl = userDoc.getString("photoUrl")

                        textUsername.text = name

                        Glide.with(this)
                            .load(profileUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(ivProfile)
                    }
            }

        return view
    }

    // 🔹 시간차를 문자열로 변환하는 함수 (예: "5분 전", "2시간 전")
    private fun convertTimestampToTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / 1000 / 60
        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            minutes < 60 * 24 -> "${minutes / 60}시간 전"
            else -> "${minutes / 60 / 24}일 전"
        }
    }
}