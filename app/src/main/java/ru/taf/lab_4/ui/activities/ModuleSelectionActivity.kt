package ru.taf.lab_4.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.taf.lab_4.databinding.ActivityModuleSelectionBinding

class ModuleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка TopBar
        binding.topBar.moduleInfoContainer.visibility = View.VISIBLE
        binding.topBar.tvRoleName.text = "Администратор"

        // В реальном приложении здесь была бы обработка кликов по таблице и кнопке
    }
}