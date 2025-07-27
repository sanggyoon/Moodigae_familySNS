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
import com.example.familysns.util.PrefsHelper
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.firestore.SetOptions

class PostDetailFragment : Fragment() {

    private lateinit var imageSlider: ViewPager2
    private lateinit var textUsername: TextView
    private lateinit var textMessage: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var textTime: TextView

    // 이모지 카운트 텍스트
    private lateinit var heartCount: TextView
    private lateinit var thumbUpCount: TextView
    private lateinit var smileCount: TextView
    private lateinit var sadCount: TextView
    private lateinit var upsetCount: TextView
    private lateinit var surpriseCount: TextView

    // 이모지 버튼
    private lateinit var btnHeart: View
    private lateinit var btnThumbUp: View
    private lateinit var btnSmile: View
    private lateinit var btnSad: View
    private lateinit var btnUpset: View
    private lateinit var btnSurprise: View

    private lateinit var postId: String
    private lateinit var familyId: String
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_detail, container, false)

        // XML 뷰 연결
        imageSlider = view.findViewById(R.id.image_slider)
        textUsername = view.findViewById(R.id.tv_username)
        textMessage = view.findViewById(R.id.tv_message)
        ivProfile = view.findViewById(R.id.iv_profile)
        textTime = view.findViewById(R.id.tv_time)

        heartCount = view.findViewById(R.id.count_heart)
        thumbUpCount = view.findViewById(R.id.count_thumb_up)
        smileCount = view.findViewById(R.id.count_smile)
        sadCount = view.findViewById(R.id.count_sad)
        upsetCount = view.findViewById(R.id.count_upset)
        surpriseCount = view.findViewById(R.id.count_surprise)

        btnHeart = view.findViewById(R.id.btn_heart)
        btnThumbUp = view.findViewById(R.id.btn_thumb_up)
        btnSmile = view.findViewById(R.id.btn_smile)
        btnSad = view.findViewById(R.id.btn_sad)
        btnUpset = view.findViewById(R.id.btn_upset)
        btnSurprise = view.findViewById(R.id.btn_surprise)

        // Safe Args
        val args = PostDetailFragmentArgs.fromBundle(requireArguments())
        postId = args.postId
        familyId = args.familyId

        userId = PrefsHelper.getUserId(requireContext()) ?: ""

        val db = FirebaseFirestore.getInstance()

        // 게시글 정보 불러오기
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

                if (imageUrls.isNotEmpty()) {
                    val adapter = ImageSliderAdapter(imageUrls)
                    imageSlider.adapter = adapter
                }

                // 작성자 정보
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

        // 리액션 로딩
        loadReactions()

        // 클릭 리스너 등록
        btnHeart.setOnClickListener { toggleReaction("heart") }
        btnThumbUp.setOnClickListener { toggleReaction("thumbUp") }
        btnSmile.setOnClickListener { toggleReaction("smile") }
        btnSad.setOnClickListener { toggleReaction("sad") }
        btnUpset.setOnClickListener { toggleReaction("upset") }
        btnSurprise.setOnClickListener { toggleReaction("surprise") }

        return view
    }

    private fun toggleReaction(reactionType: String) {
        val docRef = FirebaseFirestore.getInstance()
            .collection("families")
            .document(familyId)
            .collection("posts")
            .document(postId)
            .collection("reaction")
            .document("reactionId")

        val countTextView = when (reactionType) {
            "heart" -> heartCount
            "thumbUp" -> thumbUpCount
            "smile" -> smileCount
            "sad" -> sadCount
            "upset" -> upsetCount
            "surprise" -> surpriseCount
            else -> return
        }

        // 먼저 UI에서 숫자 변경
        val current = countTextView.text.toString().toInt()
        val userId = this.userId

        docRef.get().addOnSuccessListener { snapshot ->
            val currentList = snapshot.get(reactionType) as? List<String> ?: emptyList()
            val isLiked = currentList.contains(userId)
            val updatedList = if (isLiked) currentList - userId else currentList + userId

            // UI 즉시 반영
            val newCount = if (isLiked) currentList.size - 1 else currentList.size + 1
            countTextView.text = newCount.toString()

            // 문서가 없을 경우 대비해 set + merge로 작성
            docRef.set(mapOf(reactionType to updatedList), SetOptions.merge())
                .addOnFailureListener {
                    countTextView.text = currentList.size.toString()
                }
        }
    }

    private fun loadReactions() {
        val docRef = FirebaseFirestore.getInstance()
            .collection("families")
            .document(familyId)
            .collection("posts")
            .document(postId)
            .collection("reaction")
            .document("reactionId")

        docRef.get().addOnSuccessListener { doc ->
            heartCount.text = (doc["heart"] as? List<*>)?.size?.toString() ?: "0"
            thumbUpCount.text = (doc["thumbUp"] as? List<*>)?.size?.toString() ?: "0"
            smileCount.text = (doc["smile"] as? List<*>)?.size?.toString() ?: "0"
            sadCount.text = (doc["sad"] as? List<*>)?.size?.toString() ?: "0"
            upsetCount.text = (doc["upset"] as? List<*>)?.size?.toString() ?: "0"
            surpriseCount.text = (doc["surprise"] as? List<*>)?.size?.toString() ?: "0"
        }
    }

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