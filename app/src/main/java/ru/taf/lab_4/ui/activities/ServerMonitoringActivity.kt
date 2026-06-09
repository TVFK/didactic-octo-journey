package ru.taf.lab_4.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import ru.taf.lab_4.R
import ru.taf.lab_4.api.RetrofitClient
import ru.taf.lab_4.databinding.ActivityServerMonitoringBinding
import ru.taf.lab_4.model.ModuleInfo
import ru.taf.lab_4.model.ModuleStatusResponse

class ServerMonitoringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerMonitoringBinding
    private var allModules: List<ModuleInfo> = emptyList()
    private var currentFilter: String? = null // null = все, "online", "offline"
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                updateTable()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.filterGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentFilter = when (checkedId) {
                R.id.btnFilterOnline -> "online"
                R.id.btnFilterOffline -> "offline"
                else -> null
            }
            updateTable()
        }

        loadMonitoringData()
    }

    private fun loadMonitoringData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.moduleApi.getModulesStatus()
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        updateStats(data)
                        allModules = data.modules
                        updateTable()
                    }
                }
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    private fun updateStats(data: ModuleStatusResponse) {
        binding.tvTotalModules.text = data.totalModules.toString()
        binding.tvOnlineCount.text = data.workingModules.toString()
        binding.tvOfflineCount.text = data.notWorkingModules.toString()
        binding.tvErrorCount.text = data.modulesWithUnavailableCamera.toString()
    }

    private fun updateTable() {
        val table = binding.moduleTable
        // Удаляем все, кроме заголовка и разделителя (первые два дочерних элемента)
        while (table.childCount > 2) {
            table.removeViewAt(table.childCount - 1)
        }

        val filtered = allModules.filter { module ->
            val matchesFilter = when (currentFilter) {
                "online" -> module.status == "online"
                "offline" -> module.status == "offline"
                else -> true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    module.moduleCode.contains(searchQuery, ignoreCase = true) ||
                    module.stationName.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }

        for (module in filtered) {
            addModuleRow(module)
        }
    }

    private fun addModuleRow(module: ModuleInfo) {
        val row = TableRow(this).apply {
            background = ContextCompat.getDrawable(this@ServerMonitoringActivity, R.drawable.selector_table_row)
        }

        // ID модуля
        row.addView(createCell(module.moduleCode, bold = true))

        // Столовая
        row.addView(createCell(module.stationName))

        // Статус (chip)
        val statusChip = Chip(this).apply {
            text = module.status.uppercase()
            textSize = 12f
            setChipMinHeight(dpToPx(28).toFloat())
            setEnsureMinTouchTargetSize(false)
            if (module.status == "online") {
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.online_text))
                chipBackgroundColor = ContextCompat.getColorStateList(this@ServerMonitoringActivity, R.color.online_bg)
                chipIcon = ContextCompat.getDrawable(this@ServerMonitoringActivity, R.drawable.ic_circle_small)
                chipIconTint = ContextCompat.getColorStateList(this@ServerMonitoringActivity, R.color.online_text)
            } else {
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.offline_text))
                chipBackgroundColor = ContextCompat.getColorStateList(this@ServerMonitoringActivity, R.color.offline_bg)
                chipIcon = ContextCompat.getDrawable(this@ServerMonitoringActivity, R.drawable.ic_circle_small)
                chipIconTint = ContextCompat.getColorStateList(this@ServerMonitoringActivity, R.color.offline_text)
            }
        }
        val statusFrame = FrameLayout(this).apply {
            addView(statusChip)
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
        }
        row.addView(statusFrame)

        // Режим
        if (module.status == "online") {
            val modeChip = Chip(this).apply {
                text = module.mode.uppercase()
                textSize = 12f
                setChipMinHeight(dpToPx(28).toFloat())
                setEnsureMinTouchTargetSize(false)
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.mode_text))
                chipBackgroundColor = ContextCompat.getColorStateList(this@ServerMonitoringActivity, R.color.mode_bg)
            }
            val modeFrame = FrameLayout(this).apply {
                addView(modeChip)
                setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            }
            row.addView(modeFrame)
        } else {
            row.addView(createCell("—", grey = true))
        }

        // Камера
        val cameraText = when {
            module.status == "online" && module.cameraStatus == "active" -> "Активна"
            module.status == "online" -> module.cameraStatus
            else -> "—"
        }
        row.addView(createCell(cameraText, grey = module.status != "online"))

        binding.moduleTable.addView(row)

        // Разделитель
        val separator = View(this).apply {
            layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpToPx(1))
            setBackgroundColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.separator))
        }
        binding.moduleTable.addView(separator)
    }

    private fun createCell(text: String, bold: Boolean = false, grey: Boolean = false): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            if (bold) {
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.text_primary))
            } else if (grey) {
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.text_grey))
            } else {
                setTextColor(ContextCompat.getColor(this@ServerMonitoringActivity, R.color.text_primary))
            }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
