package ru.alex.lab_4.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import ru.alex.lab_4.ui.activities.DishDatabaseActivity
import ru.taf.lab_4.databinding.LayoutNavigationPanelBinding
import ru.taf.lab_4.ui.activities.DashboardActivity
import ru.taf.lab_4.ui.activities.LoginActivity
import ru.taf.lab_4.ui.activities.ModuleSelectionActivity
import ru.taf.lab_4.ui.activities.ServerMonitoringActivity

object NavigationUtils {

    fun setupNavigation(activity: Activity, navBinding: LayoutNavigationPanelBinding) {
        navBinding.navDashboard.setOnClickListener {
            if (activity !is DashboardActivity) {
                navigateTo(activity, DashboardActivity::class.java)
            }
        }

        navBinding.navModules.setOnClickListener {
            if (activity !is ModuleSelectionActivity) {
                navigateTo(activity, ModuleSelectionActivity::class.java)
            }
        }

        navBinding.navDishes.setOnClickListener {
            if (activity !is DishDatabaseActivity) {
                navigateTo(activity, DishDatabaseActivity::class.java)
            }
        }

        navBinding.navResourceMonitoring.setOnClickListener {
            if (activity !is ServerMonitoringActivity) {
                navigateTo(activity, ServerMonitoringActivity::class.java)
            }
        }

        navBinding.navLogout.setOnClickListener {
            logout(activity)
        }
    }

    private fun <T : Activity> navigateTo(currentActivity: Activity, targetClass: Class<T>) {
        val intent = Intent(currentActivity, targetClass)
        currentActivity.startActivity(intent)
        // Не вызываем finish(), чтобы можно было вернуться назад, 
        // или вызываем, если это "корневые" экраны. 
        // Для демонстрационного проекта обычно переходы между табами заменяют текущий стек.
        currentActivity.finish()
    }

    fun logout(activity: Activity) {
        activity.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
        activity.finish()
    }
}
