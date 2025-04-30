package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import ru.taf.lab_4.R
import ru.taf.lab_4.data.AppDatabaseHelper
import ru.taf.lab_4.databinding.ActivityProfileBinding
import ru.taf.lab_4.model.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var authPrefs: SharedPreferences
    private val dbHelper by lazy { AppDatabaseHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        authPrefs = getSharedPreferences("auth", MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean("DarkTheme", false)
        setAppTheme(isDarkTheme)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupThemeSwitch(isDarkTheme)
        setupUserInfo()
        setupStats()
    }

    override fun onResume() {
        super.onResume()
        setupStats()
    }

    private fun setupThemeSwitch(isDarkTheme: Boolean) {
        binding.themeSwitch.isChecked = isDarkTheme
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("DarkTheme", isChecked) }
            setAppTheme(isChecked)
            recreate()
        }
    }

    private fun setupUserInfo() {
        intent.getParcelableExtra("user", User::class.java)?.let { user ->
            with(binding) {
                tvFirstName.text = getString(R.string.first_name_label, user.name)
                tvLastName.text = getString(R.string.last_name_label, user.surname)
                tvEmail.text = getString(R.string.email_label, user.email)

                btnToQuestions.setOnClickListener {
                    startActivity(Intent(this@ProfileActivity, WordLearningActivity::class.java))
                }
            }
        } ?: run {
            Toast.makeText(this, "Данные пользователя не найдены", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupStats() {
        val userId = authPrefs.getInt("userId", -1)
        if (userId == -1) {
            binding.tvStats.text = "Пользователь не авторизован"
            return
        }

        val stats = dbHelper.getStats(userId)
        binding.tvStats.text = if (stats.total > 0) {
            "Изучено слов: ${stats.total}\nПравильных: ${stats.correct} (${"%.1f".format(stats.correct.toFloat() / stats.total * 100)}%)"
        } else {
            "Статистика пока недоступна"
        }
    }

    private fun setAppTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}