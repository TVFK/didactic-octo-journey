package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.taf.lab_4.api.RetrofitClient
import ru.taf.lab_4.databinding.ActivityLoginBinding
import ru.taf.lab_4.model.LoginRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.errorBanner.visibility = View.GONE

        binding.btnLogin.setOnClickListener {
            val username = binding.etLogin.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.errorBanner.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.authApi.login(
                        LoginRequest(username, password)
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit {
                                putString("access_token", body.access_token)
                                putString("refresh_token", body.refresh_token)
                                apply()
                            }
                            // Переход на SuccessActivity вместо прямого редиректа
                            val intent =
                                Intent(this@LoginActivity, LoginSuccessActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            showError("Пустой ответ от сервера")
                        }
                    } else {
                        showError("Неверный логин или пароль")
                    }
                } catch (e: Exception) {
                    showError("Ошибка сети: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvErrorText.text = message
        binding.errorBanner.visibility = View.VISIBLE
    }
}