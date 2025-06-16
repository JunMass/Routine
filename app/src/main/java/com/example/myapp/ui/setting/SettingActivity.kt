package com.example.myapp.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val userId = prefs.getString("userId", "알 수 없음")

        val userIdTextView = findViewById<TextView>(R.id.userIdTextView)
        userIdTextView.text = "현재 로그인된 사용자: $userId"
        // 로그아웃 버튼
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            getSharedPreferences("loginPrefs", MODE_PRIVATE)
                .edit().putBoolean("isLoggedIn", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        changePasswordButton.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // 뒤로 가기 버튼
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
