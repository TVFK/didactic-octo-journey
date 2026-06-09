package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.taf.lab_4.api.RetrofitClient
import ru.taf.lab_4.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загрузка данных статуса
        loadStatus()

        // Карточка "Войти в систему"
        binding.cardLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Карточка "Мониторинг"
        binding.cardMonitoring.setOnClickListener {
            startActivity(Intent(this, ServerMonitoringActivity::class.java))
        }
    }

    private fun loadStatus() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.moduleApi.getModulesStatus()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        binding.tvActiveModules.text =
                            "${data.workingModules} / ${data.totalModules}"
                        binding.tvSyncStatus.text =
                            if (data.workingModules > 0) "В порядке" else "Нет связи"
                        // Последнее обновление меню - пока оставим хардкод, нет данных в API
                        binding.tvLastUpdate.text = "24.05.2026 08:30"
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}