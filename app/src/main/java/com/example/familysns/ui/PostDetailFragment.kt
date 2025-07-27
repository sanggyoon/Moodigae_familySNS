package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.familysns.R
import com.example.familysns.adapter.ImageSliderAdapter
import com.example.familysns.util.PrefsHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import de.hdodenhof.circleimageview.CircleImageView

class PostDetailFragment : Fragment() {

    private lateinit var imageSlider: ViewPager2
    private lateinit var textUsername: TextView
    private lateinit var textMessage: TextView
    private lateinit var ivProfile: CircleImageView
    private lateinit var textTime: TextView

    // 댓글
    private lateinit var commentContainer: LinearLayout
    private lateinit var editComment: EditText
    private lateinit var btnSendComment: ImageButton

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

        imageSlider = view.findViewById(R.id.image_slider)
        textUsername = view.findViewById(R.id.tv_username)
        textMessage = view.findViewById(R.id.tv_message)
        ivProfile = view.findViewById(R.id.iv_profile)
        textTime = view.findViewById(R.id.tv_time)

        commentContainer = view.findViewById(R.id.comment_container)
        editComment = view.findViewById(R.id.edit_comment)
        btnSendComment = view.findViewById(R.id.btn_send_comment)

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

        val args = PostDetailFragmentArgs.fromBundle(requireArguments())
        postId = args.postId
        familyId = args.familyId
        userId = PrefsHelper.getUserId(requireContext()) ?: ""

        val db = FirebaseFirestore.getInstance()

        db.collection("families").document(familyId)
            .collection("posts").document(postId)
            .get().addOnSuccessListener { doc ->
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

                db.collection("users").document(authorId)
                    .get().addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("name") ?: "작성자"
                        val profileUrl = userDoc.getString("photoUrl")
                        textUsername.text = name
                        Glide.with(this).load(profileUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop().into(ivProfile)
                    }
            }

        loadReactions()
        loadComments()

        btnHeart.setOnClickListener { toggleReaction("heart") }
        btnThumbUp.setOnClickListener { toggleReaction("thumbUp") }
        btnSmile.setOnClickListener { toggleReaction("smile") }
        btnSad.setOnClickListener { toggleReaction("sad") }
        btnUpset.setOnClickListener { toggleReaction("upset") }
        btnSurprise.setOnClickListener { toggleReaction("surprise") }

        btnSendComment.setOnClickListener {
            val comment = editComment.text.toString().trim()
            if (comment.isNotEmpty()) sendComment(comment)
        }

        return view
    }

    private fun toggleReaction(reactionType: String) {
        val docRef = FirebaseFirestore.getInstance()
            .collection("families").document(familyId)
            .collection("posts").document(postId)
            .collection("reaction").document("reactionId")

        val countTextView = when (reactionType) {
            "heart" -> heartCount
            "thumbUp" -> thumbUpCount
            "smile" -> smileCount
            "sad" -> sadCount
            "upset" -> upsetCount
            "surprise" -> surpriseCount
            else -> return
        }

        val current = countTextView.text.toString().toInt()
        val userId = this.userId

        docRef.get().addOnSuccessListener { snapshot ->
            val currentList = snapshot.get(reactionType) as? List<String> ?: emptyList()
            val isLiked = currentList.contains(userId)
            val updatedList = if (isLiked) currentList - userId else currentList + userId
            val newCount = if (isLiked) currentList.size - 1 else currentList.size + 1
            countTextView.text = newCount.toString()

            docRef.set(mapOf(reactionType to updatedList), SetOptions.merge())
                .addOnFailureListener {
                    countTextView.text = currentList.size.toString()
                }
        }
    }

    private fun loadReactions() {
        val docRef = FirebaseFirestore.getInstance()
            .collection("families").document(familyId)
            .collection("posts").document(postId)
            .collection("reaction").document("reactionId")

        docRef.get().addOnSuccessListener { doc ->
            heartCount.text = (doc["heart"] as? List<*>)?.size?.toString() ?: "0"
            thumbUpCount.text = (doc["thumbUp"] as? List<*>)?.size?.toString() ?: "0"
            smileCount.text = (doc["smile"] as? List<*>)?.size?.toString() ?: "0"
            sadCount.text = (doc["sad"] as? List<*>)?.size?.toString() ?: "0"
            upsetCount.text = (doc["upset"] as? List<*>)?.size?.toString() ?: "0"
            surpriseCount.text = (doc["surprise"] as? List<*>)?.size?.toString() ?: "0"
        }
    }

    private fun sendComment(commentText: String) {
        val db = FirebaseFirestore.getInstance()
        val commentRef = db.collection("families").document(familyId)
            .collection("posts").document(postId)
            .collection("comment").document()

        val commentData = mapOf(
            "authorId" to userId,
            "comment" to commentText,
            "createdAt" to FieldValue.serverTimestamp()
        )

        commentRef.set(commentData).addOnSuccessListener {
            editComment.setText("")
            loadComments()
        }
    }

    private fun loadComments() {
        val db = FirebaseFirestore.getInstance()
        val commentRef = db.collection("families").document(familyId)
            .collection("posts").document(postId)
            .collection("comment")

        commentRef.orderBy("createdAt", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { snapshot ->
                commentContainer.removeAllViews()
                for (doc in snapshot.documents) {
                    val commentText = doc.getString("comment") ?: ""
                    val authorId = doc.getString("authorId") ?: ""
                    db.collection("users").document(authorId).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "익명"
                            val photoUrl = userDoc.getString("photoUrl")
                            val itemView = layoutInflater.inflate(R.layout.item_comment, null)
                            val ivProfile = itemView.findViewById<CircleImageView>(R.id.iv_comment_profile)
                            val tvName = itemView.findViewById<TextView>(R.id.tv_comment_name)
                            val tvComment = itemView.findViewById<TextView>(R.id.tv_comment_text)
                            tvName.text = name
                            tvComment.text = commentText
                            Glide.with(this).load(photoUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .circleCrop().into(ivProfile)
                            commentContainer.addView(itemView)
                        }
                }
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