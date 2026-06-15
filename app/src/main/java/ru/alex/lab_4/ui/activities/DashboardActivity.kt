package ru.taf.lab_4.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.alex.lab_4.utils.NavigationUtils
import ru.taf.lab_4.R
import ru.taf.lab_4.databinding.ActivityDashboardBinding
import ru.taf.lab_4.ui.fragments.DashboardFragment
import ru.taf.lab_4.ui.fragments.ModuleSelectionFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка TopBar
        binding.topBar.moduleInfoContainer.visibility = View.VISIBLE
        binding.topBar.tvRoleName.text = "Администратор"

        // Установка фрагмента по умолчанию
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment(), "dashboard")
            binding.navigationPanel.navDashboard.isSelected = true
        }

        // Обработка нажатия на "Панель состояния"
        binding.navigationPanel.navDashboard.setOnClickListener {
            replaceFragment(DashboardFragment(), "dashboard")
            resetNavigationSelection()
            binding.navigationPanel.navDashboard.isSelected = true
        }

        // Обработка нажатия на "Выбор модуля распознавания"
        binding.navigationPanel.navModules.setOnClickListener {
            replaceFragment(ModuleSelectionFragment(), "modules")
            resetNavigationSelection()
            binding.navigationPanel.navModules.isSelected = true
        }

        // Настройка общей навигации (для остальных пунктов)
        NavigationUtils.setupNavigation(this, binding.navigationPanel)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment, tag)
            .commit()
    }

    private fun resetNavigationSelection() {
        binding.navigationPanel.navDashboard.isSelected = false
        binding.navigationPanel.navModules.isSelected = false
        // Добавьте другие пункты меню здесь
    }
}
