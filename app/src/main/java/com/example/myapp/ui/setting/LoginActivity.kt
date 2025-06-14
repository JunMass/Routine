package com.example.myapp.ui.setting

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
//import androidx.biometric.BiometricManager
//import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.myapp.MainActivity
import com.example.myapp.R
import java.security.MessageDigest
import java.util.concurrent.Executor

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
            val userId = idInput.text.toString()
            val inputPassword = passwordInput.text.toString()

            if (userId.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cursor = db.rawQuery(
                "SELECT hs_password FROM PasswordTable WHERE user_id = ?",
                arrayOf(userId)
            )

            if (cursor.moveToFirst()) {
                val savedHashedPassword = cursor.getString(0)
                val inputHashed = hashPassword(inputPassword)
                Log.d("LoginCheck", "inputHashed: $inputHashed, savedHashedPassword: $savedHashedPassword")
                if (inputHashed == savedHashedPassword) {
                    prefs.edit().putBoolean("isLoggedIn", true)
                        .putString("userId", userId)
                        .apply()

                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "존재하지 않는 아이디입니다", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
        }

//        val biometricManager = BiometricManager.from(this)
//        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
//            biometricLoginButton.isEnabled = false
//            biometricLoginButton.alpha = 0.5f
//        }
//
//        executor = ContextCompat.getMainExecutor(this)
//        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
//            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                super.onAuthenticationSucceeded(result)
//                prefs.edit().putBoolean("isLoggedIn", true).apply()
//                Toast.makeText(applicationContext, "지문 인증 성공", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//                finish()
//            }
//
//            override fun onAuthenticationFailed() {
//                super.onAuthenticationFailed()
//                Toast.makeText(applicationContext, "지문 인증 실패", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("지문 인증")
//            .setSubtitle("등록된 지문으로 로그인합니다")
//            .setNegativeButtonText("취소")
//            .build()
//
//        biometricLoginButton.setOnClickListener {
//            biometricPrompt.authenticate(promptInfo)
//        }
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
