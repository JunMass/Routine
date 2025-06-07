package com.example.myapp.ui.setting

import android.content.ContentValues
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import java.security.MessageDigest

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var currentPasswordEdit: EditText
    private lateinit var newPasswordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var currentPasswordDisplay: TextView
    private lateinit var saveButton: Button
    private lateinit var db: SQLiteDatabase
    private lateinit var prefs: SharedPreferences
    private var savedUserId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        savedUserId = prefs.getString("userId", "")
        currentPasswordEdit = findViewById(R.id.currentPasswordInput)
        newPasswordEdit = findViewById(R.id.newPasswordInput)
        confirmPasswordEdit = findViewById(R.id.confirmPasswordInput)
        saveButton = findViewById(R.id.savePasswordButton)
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        currentPasswordDisplay = findViewById(R.id.currentPasswordDisplay)

        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val method = if (isChecked)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
            currentPasswordEdit.transformationMethod = method
            newPasswordEdit.transformationMethod = method
            confirmPasswordEdit.transformationMethod = method

            // 커서 끝으로 이동
            currentPasswordEdit.setSelection(currentPasswordEdit.text.length)
            newPasswordEdit.setSelection(newPasswordEdit.text.length)
            confirmPasswordEdit.setSelection(confirmPasswordEdit.text.length)
        }

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
        showCurrentPassword()

        saveButton.setOnClickListener {
            val currentPw = currentPasswordEdit.text.toString()
            val newPw = newPasswordEdit.text.toString()
            val confirmPw = confirmPasswordEdit.text.toString()

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "모든 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DB에서 저장된 비밀번호 불러오기 (id=1 가정)
            val cursor = db.rawQuery("SELECT password, hs_password FROM PasswordTable WHERE user_id=?",
                arrayOf(savedUserId))
            var storedPw: String? = null
            var storedHsPw: String? = null
            if (cursor.moveToFirst()) {
                storedPw = cursor.getString(0)
                storedHsPw = cursor.getString(1)
            }
            cursor.close()

            if (storedPw == null) {
                Toast.makeText(this, "저장된 비밀번호가 없습니다", Toast.LENGTH_SHORT).show()
               // return@setOnClickListener
            }

            // 현재 입력한 비밀번호 해시화 비교
            if (hashPassword(currentPw) != storedHsPw) {
                Toast.makeText(this, "현재 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 새 비밀번호 확인
            if (newPw != confirmPw) {
                Toast.makeText(this, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 비밀번호 변경 저장
            val values = ContentValues().apply {
                put("password", newPw)           // 평문 (디버깅용)
                put("hs_password", hashPassword(newPw))
            }
            val updatedRows = db.update(
                "PasswordTable",
                values,
                "user_id = ?",
                arrayOf(savedUserId)
            )

            if (updatedRows > 0) {
                Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "비밀번호 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
            }

        }
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
    private fun showCurrentPassword() {
        val cursor = db.rawQuery(
            "SELECT password FROM PasswordTable WHERE user_id = ?",
            arrayOf(savedUserId)
        )
        if (cursor.moveToFirst()) {
            val pw = cursor.getString(0)
            currentPasswordDisplay.text = "현재 비밀번호: $pw"
        } else {
            currentPasswordDisplay.text = "현재 비밀번호가 설정되지 않았습니다."
        }
        cursor.close()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
