package com.example.familysns

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.familysns.ui.AuthActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // 로그인된 유저: MainActivity로
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 비로그인 유저: AuthActivity로
            startActivity(Intent(this, AuthActivity::class.java))
        }

        finish()
    }
}