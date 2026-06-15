package ru.taf.lab_4.ui.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import ru.taf.lab_4.R
import ru.taf.lab_4.api.RetrofitClient
import ru.alex.lab_4.utils.NavigationUtils
import ru.taf.lab_4.databinding.ActivityModuleSelectionBinding
import ru.taf.lab_4.model.ModuleDetail
import ru.taf.lab_4.model.ModuleSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ModuleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModuleSelectionBinding

    // module_id → TableRow: нужен для переключения выделения без пересоздания строк
    private val rowMap = mutableMapOf<Int, TableRow>()
    private var selectedModuleId: Int? = null

    private val handler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            binding.topBar.time.text =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            handler.postDelayed(this, 60_000L)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        setupClock()
        setupNavigation()
        loadModules()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
    }

    // ──────────────────────────────────────────────────────────────
    // Setup
    // ──────────────────────────────────────────────────────────────

    private fun setupTopBar() {
        binding.topBar.moduleInfoContainer.visibility = View.VISIBLE
        binding.topBar.tvRoleName.text = "Администратор"
    }

    private fun setupClock() {
        handler.post(clockRunnable)
    }

    private fun setupNavigation() {
        // Подсвечиваем активный пункт
        binding.navigationPanel.navModules.isSelected = true
        NavigationUtils.setupNavigation(this, binding.navigationPanel)
    }

    // ──────────────────────────────────────────────────────────────
    // Загрузка списка модулей (левая/центральная таблица)
    // ──────────────────────────────────────────────────────────────

    private fun loadModules() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.moduleApi.getModulesStatusAuth()
                if (response.isSuccessful) {
                    val modules = response.body()?.modules.orEmpty()
                    populateTable(modules)
                    // Автовыбор первого модуля
                    if (modules.isNotEmpty()) {
                        selectModule(modules[0])
                    }
                } else {
                    showError("Ошибка загрузки списка: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Нет соединения: ${e.localizedMessage}")
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Выбор строки таблицы
    // ──────────────────────────────────────────────────────────────

    private fun selectModule(module: ModuleSummary) {
        selectedModuleId = module.moduleId
        updateRowHighlight()
        loadModuleDetail(module.moduleId)
    }

    /** Обновляет фон только у затронутых строк — без пересоздания таблицы */
    private fun updateRowHighlight() {
        rowMap.forEach { (moduleId, row) ->
            row.setBackgroundResource(
                if (moduleId == selectedModuleId) R.drawable.bg_table_row_selected
                else R.drawable.selector_table_row
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Построение таблицы
    // ──────────────────────────────────────────────────────────────

    private fun populateTable(modules: List<ModuleSummary>) {
        rowMap.clear()

        // Удаляем все дочерние элементы после строки-заголовка (index 0)
        // и разделителя под ней (index 1)
        val childCount = binding.moduleTable.childCount
        if (childCount > 2) {
            binding.moduleTable.removeViews(2, childCount - 2)
        }

        modules.forEachIndexed { index, module ->
            if (index > 0) {
                binding.moduleTable.addView(createDividerView())
            }
            val row = createModuleRow(module)
            rowMap[module.moduleId] = row
            binding.moduleTable.addView(row)
        }
    }

    private fun createModuleRow(module: ModuleSummary): TableRow {
        val row = TableRow(this).apply {
            setBackgroundResource(R.drawable.selector_table_row)
            isClickable = true
            isFocusable = true
        }

        row.addView(createTextCell(module.moduleCode, bold = true, textColor = "#212121"))
        row.addView(createTextCell(module.stationName))
        row.addView(createTextCell(module.url, monospace = true))
        row.addView(createModeCell(module.mode))
        row.addView(createStatusCell(module.status))
        row.addView(createTextCell(module.modelVersion, monospace = true))
        row.addView(createTextCell(formatDate(module.lastSeenAt)))

        row.setOnClickListener { selectModule(module) }

        return row
    }

    // ──────────────────────────────────────────────────────────────
    // Вспомогательные View-фабрики для ячеек таблицы
    // ──────────────────────────────────────────────────────────────

    private fun createTextCell(
        text: String,
        bold: Boolean = false,
        monospace: Boolean = false,
        textColor: String = "#424242"
    ): TextView = TextView(this).apply {
        layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        setPadding(12.dp, 12.dp, 12.dp, 12.dp)
        this.text = text
        setTextColor(Color.parseColor(textColor))
        textSize = 14f
        if (bold) setTypeface(typeface, Typeface.BOLD)
        if (monospace) typeface = Typeface.MONOSPACE
    }

    /** mode != "—" → синий chip; "—" → серый текст */
    private fun createModeCell(mode: String): View =
        if (mode.isNotBlank() && mode != "—") {
            createChipCell(
                text      = mode,
                textColor = "#1976d2",
                bgColor   = "#e3f2fd"
            )
        } else {
            createTextCell("—", textColor = "#9e9e9e")
        }

    /** ONLINE → зелёный; OFFLINE → красный, с иконкой ic_circle_small */
    private fun createStatusCell(status: String): FrameLayout {
        val online = status.uppercase() == "ONLINE"
        return createChipCell(
            text      = status,
            textColor = if (online) "#4caf50" else "#f44336",
            bgColor   = if (online) "#e8f5e9" else "#ffebee",
            withIcon  = true,
            iconTint  = if (online) "#4caf50" else "#f44336"
        )
    }

    private fun createChipCell(
        text: String,
        textColor: String,
        bgColor: String,
        withIcon: Boolean = false,
        iconTint: String? = null
    ): FrameLayout {
        val chip = Chip(this).apply {
            this.text = text
            setTextColor(Color.parseColor(textColor))
            chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(bgColor))
            chipMinHeight = 28f.dp
            this.textSize = 12f
            isClickable = false
            isFocusable = false
            if (withIcon && iconTint != null) {
                setChipIconResource(R.drawable.ic_circle_small)
                this.chipIconTint = ColorStateList.valueOf(Color.parseColor(iconTint))
            }
        }

        val chipLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        return FrameLayout(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            addView(chip, chipLp)
        }
    }

    private fun createDividerView(): View = View(this).apply {
        layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1.dp)
        setBackgroundColor(Color.parseColor("#e0e0e0"))
    }

    // ──────────────────────────────────────────────────────────────
    // Загрузка и отображение детальной информации (правая панель)
    // ──────────────────────────────────────────────────────────────

    private fun loadModuleDetail(moduleId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.moduleApi.getModuleDetail(moduleId)
                if (response.isSuccessful) {
                    response.body()?.let { updateInfoPanel(it) }
                } else {
                    showError("Ошибка загрузки модуля: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Нет соединения: ${e.localizedMessage}")
            }
        }
    }

    private fun updateInfoPanel(module: ModuleDetail) {
        binding.tvInfoModuleId.text      = module.moduleCode
        binding.tvInfoCanteen.text       = module.stationName
        binding.tvInfoIp.text            = module.url
        binding.tvInfoVersion.text       = module.modelVersion
        binding.tvInfoLastConnection.text = formatDate(module.lastSeenAt)

        // Статус-чип
        val online = module.status.uppercase() == "ONLINE"
        binding.chipInfoStatus.apply {
            text = module.status
            setTextColor(Color.parseColor(if (online) "#4caf50" else "#f44336"))
            chipBackgroundColor = ColorStateList.valueOf(
                Color.parseColor(if (online) "#e8f5e9" else "#ffebee")
            )
            chipIconTint = ColorStateList.valueOf(
                Color.parseColor(if (online) "#4caf50" else "#f44336")
            )
        }

        // Режим работы: chip или текст "—"
        if (module.mode.isNotBlank() && module.mode != "—") {
            binding.chipInfoMode.visibility  = View.VISIBLE
            binding.tvInfoModeEmpty.visibility = View.GONE
            binding.chipInfoMode.text = module.mode
        } else {
            binding.chipInfoMode.visibility  = View.GONE
            binding.tvInfoModeEmpty.visibility = View.VISIBLE
        }

        // Кнопка активна только для ONLINE-модулей
        binding.btnGoToServer.isEnabled = online
        binding.btnGoToServer.setOnClickListener {
            // TODO: передать module_id / url на следующий экран
            // val intent = Intent(this, ModuleEventsActivity::class.java).apply {
            //     putExtra("module_id", module.moduleId)
            //     putExtra("module_url", module.url)
            // }
            // startActivity(intent)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Вспомогательные утилиты
    // ──────────────────────────────────────────────────────────────

    /** ISO-8601 ("2026-06-14T16:37:39.031Z") → "14.06.2026 16:37" */
    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        val output = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        for (pattern in formats) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                return output.format(parser.parse(isoDate) ?: continue)
            } catch (_: Exception) { }
        }
        return isoDate
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // dp-расширения внутри Activity (resources доступен из контекста)
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = this * resources.displayMetrics.density
}