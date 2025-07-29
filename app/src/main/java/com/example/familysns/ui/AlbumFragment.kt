package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familysns.R

class AlbumFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var btnNext: ImageView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 뷰 초기화
        recyclerView = view.findViewById(R.id.recycler_image_grid)
        btnBack = view.findViewById(R.id.btn_back)
        btnNext = view.findViewById(R.id.btn_next)
        
        // RecyclerView 설정
        setupRecyclerView()
        
        // 버튼 클릭 리스너 설정
        setupButtonListeners()
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 3) // 3열 그리드
        recyclerView.adapter = ImageGridAdapter()
    }
    
    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            // 뒤로 가기 기능
            Toast.makeText(context, "뒤로 가기", Toast.LENGTH_SHORT).show()
            // TODO: 실제 뒤로 가기 로직 구현
        }
        
        btnNext.setOnClickListener {
            // 다음 단계로 이동
            Toast.makeText(context, "다음", Toast.LENGTH_SHORT).show()
            // TODO: 실제 다음 단계 로직 구현
        }
    }
    
    // 이미지 그리드 어댑터
    inner class ImageGridAdapter : RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder>() {
        
        // 임시 데이터 (나중에 실제 이미지 데이터로 교체)
        private val imageCount = 20
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_grid, parent, false)
            return ImageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.bind(position)
        }
        
        override fun getItemCount(): Int = imageCount
        
        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.image_view)
            private val cameraIcon: ImageView = itemView.findViewById(R.id.camera_icon)
            private val selectionIndicator: ImageView = itemView.findViewById(R.id.selection_indicator)
            
            fun bind(position: Int) {
                if (position == 0) {
                    // 첫 번째 아이템은 카메라 아이콘 표시
                    imageView.setImageResource(android.R.color.darker_gray)
                    cameraIcon.visibility = View.VISIBLE
                    selectionIndicator.visibility = View.GONE
                } else {
                    // 나머지는 임시 이미지 (나중에 실제 이미지로 교체)
                    imageView.setImageResource(android.R.color.darker_gray)
                    cameraIcon.visibility = View.GONE
                    selectionIndicator.visibility = View.GONE
                }
                
                itemView.setOnClickListener {
                    if (position == 0) {
                        // 카메라 실행
                        Toast.makeText(context, "카메라 실행", Toast.LENGTH_SHORT).show()
                        // TODO: 카메라 인텐트 구현
                    } else {
                        // 이미지 선택/해제
                        if (selectionIndicator.visibility == View.VISIBLE) {
                            selectionIndicator.visibility = View.GONE
                        } else {
                            selectionIndicator.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}