package com.example.myapp.ui.setting

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var currentPasswordEdit: EditText
    private lateinit var newPasswordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var saveButton: Button
    private var savedUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        savedUserId = prefs.getString("userId", "")

        currentPasswordEdit = findViewById(R.id.currentPasswordInput)
        newPasswordEdit = findViewById(R.id.newPasswordInput)
        confirmPasswordEdit = findViewById(R.id.confirmPasswordInput)
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        saveButton = findViewById(R.id.savePasswordButton)

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val method = if (isChecked)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()

            currentPasswordEdit.transformationMethod = method
            newPasswordEdit.transformationMethod = method
            confirmPasswordEdit.transformationMethod = method

            currentPasswordEdit.setSelection(currentPasswordEdit.text.length)
            newPasswordEdit.setSelection(newPasswordEdit.text.length)
            confirmPasswordEdit.setSelection(confirmPasswordEdit.text.length)
        }

        saveButton.setOnClickListener {
            val currentPw = currentPasswordEdit.text.toString().trim()
            val newPw = newPasswordEdit.text.toString().trim()
            val confirmPw = confirmPasswordEdit.text.toString().trim()

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "모든 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPw != confirmPw) {
                Toast.makeText(this, "새 비밀번호와 확인이 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버로 비밀번호 변경 요청
            val json = JSONObject().apply {
                put("userId", savedUserId)
                put("currentPassword", currentPw)
                put("newPassword", newPw)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://routine-server-uqzh.onrender.com/change-password")
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "비밀번호 변경 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        when {
                            response.isSuccessful -> {
                                Toast.makeText(this@ChangePasswordActivity, "비밀번호가 성공적으로 변경되었습니다", Toast.LENGTH_SHORT).show()
                                // 자동 로그인 해제
                                prefs.edit().putBoolean("isLoggedIn", false).apply()
                                finish()
                            }
                            response.code == 401 -> Toast.makeText(this@ChangePasswordActivity, "현재 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@ChangePasswordActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        // 뒤로가기 버튼
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
    }
}
