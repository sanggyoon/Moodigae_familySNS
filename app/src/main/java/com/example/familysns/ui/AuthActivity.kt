package com.example.familysns.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.familysns.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        // 이 안에 로그인/회원가입 화면을 Fragment로 추가 예정
    }
}