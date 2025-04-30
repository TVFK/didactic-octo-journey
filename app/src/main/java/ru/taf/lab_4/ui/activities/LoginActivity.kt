package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import ru.taf.lab_4.data.AppDatabaseHelper
import ru.taf.lab_4.databinding.ActivityLoginBinding
import ru.taf.lab_4.model.User

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val db = AppDatabaseHelper(this)
            val user: User? = db.getUser(email, password)

            if (user == null) {
                Toast.makeText(this, "email или пароль введены неверно!", Toast.LENGTH_LONG).show()
            } else {
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit {
                    putInt("userId", user.id)
                    apply()
                }
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("user", user)
                }
                startActivity(intent)
            }
        }

        binding.toRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }
}