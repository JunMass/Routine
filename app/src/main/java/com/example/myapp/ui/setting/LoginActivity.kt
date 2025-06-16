package com.example.myapp.ui.setting

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.MainActivity
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

class LoginActivity : AppCompatActivity() {

    lateinit var db: SQLiteDatabase
    lateinit var idInput: EditText
    lateinit var passwordInput: EditText
    lateinit var loginButton: Button
    lateinit var showPasswordCheckBox: CheckBox
    lateinit var signUpButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 자동 로그인 체크
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val fromChangePassword = intent.getBooleanExtra("fromChangePassword", false)
        val savedUserId = prefs.getString("userId", "")
        if (!fromChangePassword) {
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
            if (isLoggedIn) {
                Toast.makeText(this, "${savedUserId}님 자동 로그인", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
        }

        idInput = findViewById(R.id.loginIdInput)               // 아이디 입력 필드 추가
        passwordInput = findViewById(R.id.loginPasswordInput)
        loginButton = findViewById(R.id.loginButton)
        showPasswordCheckBox = findViewById(R.id.loginShowPasswordCheckBox)
        signUpButton = findViewById(R.id.signupButton)

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
        }.readableDatabase

        showPasswordTable(db)
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordInput.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            passwordInput.setSelection(passwordInput.text.length)
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        loginButton.setOnClickListener {
            val userId = idInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("userId", userId)
                put("password", password)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://routine-server-uqzh.onrender.com/login-user")
                .post(body)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "로그인 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
                            prefs.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("userId", userId)
                                .apply()

                            Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패: 아이디 또는 비밀번호 오류", Toast.LENGTH_SHORT).show()
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
    private fun showPasswordTable(db: SQLiteDatabase) {
        val cursor = db.rawQuery("SELECT user_id, password, hs_password FROM PasswordTable", null)
        val builder = StringBuilder()
        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
                val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
                val hsPassword = cursor.getString(cursor.getColumnIndexOrThrow("hs_password"))
                builder.append("user_id: $userId\npassword: $password\nhashed: $hsPassword\n\n")
            } while (cursor.moveToNext())
        } else {
            builder.append("비밀번호 테이블에 데이터가 없습니다.")
        }
        cursor.close()

    }
}
