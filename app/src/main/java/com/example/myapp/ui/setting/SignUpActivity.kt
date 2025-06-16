package com.example.myapp.ui.setting

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import java.security.MessageDigest
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    lateinit var db: SQLiteDatabase
    lateinit var idInput: EditText
    lateinit var nicknameInput: EditText
    lateinit var passwordInput: EditText
    lateinit var passwordConfirmInput: EditText
    lateinit var signUpButton: Button
    lateinit var showPasswordCheckBox :CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        idInput = findViewById(R.id.signUpIdInput)
        nicknameInput = findViewById(R.id.signUpNicknameInput)
        passwordInput = findViewById(R.id.signUpPasswordInput)
        passwordConfirmInput = findViewById(R.id.signUpPasswordConfirmInput)
        signUpButton = findViewById(R.id.signUpButton)
        showPasswordCheckBox = findViewById(R.id.signUpShowPasswordCheckBox)

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordConfirmInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordConfirmInput.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            passwordInput.setSelection(passwordInput.text.length)
            passwordConfirmInput.setSelection(passwordConfirmInput.text.length)
        }
        // DB 초기화 및 테이블 생성
        db = object : SQLiteOpenHelper(this, "PasswordDB", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS PasswordTable (
                        user_id TEXT PRIMARY KEY,
                        password TEXT,
                        hs_password TEXT
                    )
                """.trimIndent())
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                db.execSQL("DROP TABLE IF EXISTS PasswordTable")
                onCreate(db)
            }
        }.writableDatabase

        signUpButton.setOnClickListener {
            val userId = idInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirm = passwordConfirmInput.text.toString().trim()
            val nickname = nicknameInput.text.toString().trim()  // 추가된 닉네임 필드

            if (userId.isEmpty() || password.isEmpty() || confirm.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("userId", userId)
                put("password", password)
                put("nickname", nickname)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://routine-server-uqzh.onrender.com/register-user")
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SignUpActivity, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                            finish()
                        } else if (response.code == 409) {
                            Toast.makeText(this@SignUpActivity, "이미 존재하는 ID입니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignUpActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }



    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
