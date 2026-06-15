package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.taf.lab_4.databinding.ActivityLoginSuccessBinding

class LoginSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnToDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}