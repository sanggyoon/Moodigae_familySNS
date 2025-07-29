package com.example.familysns.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.familysns.R

class FamilyHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_family_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 소식 공유하기 버튼 클릭 리스너
        view.findViewById<View>(R.id.btn_share_news).setOnClickListener {
            // TODO: 사진 선택 화면으로 이동
            // navigateToPhotoSelection()
        }
    }
}