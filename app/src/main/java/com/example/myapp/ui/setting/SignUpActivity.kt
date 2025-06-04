package com.example.myapp.ui.setting

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {

    lateinit var db: SQLiteDatabase
    lateinit var idInput: EditText
    lateinit var passwordInput: EditText
    lateinit var passwordConfirmInput: EditText
    lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        idInput = findViewById(R.id.signUpIdInput)
        passwordInput = findViewById(R.id.signUpPasswordInput)
        passwordConfirmInput = findViewById(R.id.signUpPasswordConfirmInput)
        signUpButton = findViewById(R.id.signUpButton)

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
            val userId = idInput.text.toString()
            val password = passwordInput.text.toString()
            val confirm = passwordConfirmInput.text.toString()

            if (userId.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = hashPassword(password)

            val cursor = db.rawQuery("SELECT * FROM PasswordTable WHERE user_id = ?", arrayOf(userId))
            if (cursor.moveToFirst()) {
                Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                cursor.close()
                return@setOnClickListener
            }
            cursor.close()
            db.execSQL(
                "INSERT INTO PasswordTable (user_id, password, hs_password) VALUES (?, ?, ?)",
                arrayOf(userId, password, hashedPassword)
            )
            Toast.makeText(this, "회원가입 완료", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
