package com.example.familysns.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.Timestamp
import java.util.*

class FamilyHomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var todayPostsContainer: LinearLayout
    private lateinit var weeklyPhotosContainer: LinearLayout
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
        weeklyPhotosContainer = view.findViewById(R.id.weekly_photos_container)
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
        loadWeeklyPosts()

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
                    val postId = document.id
                    val card = layoutInflater.inflate(R.layout.item_post_card, todayPostsContainer, false)
                    val ivThumbnail = card.findViewById<ImageView>(R.id.iv_thumbnail)
                    val tvMessage = card.findViewById<TextView>(R.id.tv_message)
                    val ivProfile = card.findViewById<ImageView>(R.id.iv_profile)
                    val tvUsername = card.findViewById<TextView>(R.id.tv_username)
                    val tvTime = card.findViewById<TextView>(R.id.tv_time)

                    card.setOnClickListener {
                        val action = FamilyHomeFragmentDirections
                            .actionFamilyHomeFragmentToPostDetailFragment(
                                postId = postId,
                                familyId = familyId
                            )
                        findNavController().navigate(action)
                    }

                    tvMessage.text = post.message
                    val thumbnailUrl = post.imageUrls.firstOrNull()
                    if (thumbnailUrl.isNullOrEmpty()) {
                        ivThumbnail.setImageResource(R.drawable.thumbnail_placeholder)
                    } else {
                        Glide.with(this)
                            .load(thumbnailUrl)
                            .placeholder(R.drawable.thumbnail_placeholder)
                            .error(R.drawable.thumbnail_error)
                            .centerCrop()
                            .into(ivThumbnail)
                    }

                    db.collection("users").document(post.authorId).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "알 수 없음"
                            val photoUrl = userDoc.getString("photoUrl")

                            tvUsername.text = name
                            Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(ivProfile)
                        }

                    val timestamp = post.createdAt?.toDate()?.time ?: 0L
                    tvTime.text = convertTimestampToTimeAgo(timestamp)

                    todayPostsContainer.addView(card)
                }
            }
    }

    private fun loadWeeklyPosts() {
        val familyId = PrefsHelper.getFamilyId(requireContext()) ?: return
        val days = getPast7Dates()
        weeklyPhotosContainer.removeAllViews()

        val dayImageMap = mutableListOf<Pair<Calendar, String?>>()
        var completedCount = 0

        for (day in days) {
            val start = day.clone() as Calendar
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)

            val end = day.clone() as Calendar
            end.set(Calendar.HOUR_OF_DAY, 23)
            end.set(Calendar.MINUTE, 59)
            end.set(Calendar.SECOND, 59)

            db.collection("families").document(familyId)
                .collection("posts")
                .whereGreaterThanOrEqualTo("createdAt", Timestamp(start.time))
                .whereLessThanOrEqualTo("createdAt", Timestamp(end.time))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val post = snapshot.documents.firstOrNull()
                    val imageUrls = post?.get("imageUrls") as? List<*> ?: emptyList<String>()
                    val imageUrl = imageUrls.firstOrNull() as? String

                    dayImageMap.add(day to imageUrl)
                    completedCount++

                    if (completedCount == days.size) {
                        val sortedList = dayImageMap.sortedByDescending { it.first.timeInMillis }
                        for ((d, img) in sortedList) {
                            addWeeklyPhotoView(d, img)
                        }
                    }
                }
        }
    }

    private fun addWeeklyPhotoView(day: Calendar, imageUrl: String?) {
        val itemView = layoutInflater.inflate(R.layout.item_weekly_photo, weeklyPhotosContainer, false)
        val iv = itemView.findViewById<ImageView>(R.id.weekly_image)
        val tv = itemView.findViewById<TextView>(R.id.weekly_label)

        val date = day.get(Calendar.DATE)
        val dayOfWeek = day.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.KOREAN) ?: "?"

        // 항상 날짜 + 요일 표시
        tv.text = "$date\n$dayOfWeek"

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(iv)
        } else {
            iv.setImageResource(R.drawable.bg_round_gray)
        }

        weeklyPhotosContainer.addView(itemView)
    }

    private fun getPast7Dates(): List<Calendar> {
        val list = mutableListOf<Calendar>()
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            val temp = cal.clone() as Calendar
            temp.add(Calendar.DATE, -i)
            list.add(temp)
        }
        return list
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
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 소식 공유하기 버튼 클릭 리스너
        view.findViewById<View>(R.id.btn_share_post).setOnClickListener {
            // TODO: 사진 선택 화면으로 이동
            // navigateToPhotoSelection()
        }
    }
}