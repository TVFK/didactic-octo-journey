package ru.alex.lab_4.ui.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import ru.alex.lab_4.model.DishResponse
import ru.alex.lab_4.ui.DishAdapter
import ru.alex.lab_4.utils.NavigationUtils
import ru.taf.lab_4.R
import ru.taf.lab_4.api.RetrofitClient
import ru.taf.lab_4.databinding.ActivityDishDatabaseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DishDatabaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDishDatabaseBinding
    private lateinit var adapter: DishAdapter

    private var allDishes: List<DishResponse> = emptyList()
    private var selectedCategory: String? = null  // null = "Все категории"
    private var selectedStatus: String? = null     // null = "Все статусы"

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
        binding = ActivityDishDatabaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        setupClock()
        setupNavigation()
        setupRecyclerView()
        setupFilters()
        loadDishes()
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
        binding.navigationPanel.navDishes.isSelected = true
        NavigationUtils.setupNavigation(this, binding.navigationPanel)
    }

    private fun setupRecyclerView() {
        adapter = DishAdapter { anchor, dish -> showDishMenu(anchor, dish) }

        binding.rvDishes.apply {
            layoutManager = LinearLayoutManager(this@DishDatabaseActivity)
            this.adapter = this@DishDatabaseActivity.adapter
            isNestedScrollingEnabled = false

            // Разделитель между строками (1dp, цвет #e0e0e0)
            addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                    setDrawable(ColorDrawable(Color.parseColor("#e0e0e0")))
                }
            )
        }
    }

    private fun setupFilters() {
        // Статус — статичный список
        val statuses = listOf("Все статусы", "Активно", "Неактивно")
        binding.actvStatus.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        )
        binding.actvStatus.setText("Все статусы", false)
        binding.actvStatus.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = if (position == 0) null else statuses[position]
            applyFilters()
        }

        // Поиск
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnRegisterDish.setOnClickListener {
            // TODO: startActivity(Intent(this, DishRegisterActivity::class.java))
        }
    }

    /** Строим выпадающий список категорий динамически из загруженных блюд */
    private fun buildCategoryFilter(dishes: List<DishResponse>) {
        val usedCategories = dishes
            .mapNotNull { DishAdapter.CATEGORY_MAP[it.categoryId] }
            .distinct()
            .sorted()

        val options = listOf("Все категории") + usedCategories
        binding.actvCategory.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        )
        binding.actvCategory.setText("Все категории", false)
        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = if (position == 0) null else options[position]
            applyFilters()
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Загрузка данных
    // ──────────────────────────────────────────────────────────────

    private fun loadDishes() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.dishApi.getDishes()
                println(response)
                println(response.body())
                if (response.isSuccessful) {
                    allDishes = response.body().orEmpty()
                    buildCategoryFilter(allDishes)
                    applyFilters()
                } else {
                    showError("Ошибка загрузки: ${response.code()}")
                }
            } catch (e: Exception) {
                showError("Нет соединения: ${e.localizedMessage}")
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Фильтрация (клиентская, аналогично React-версии)
    // ──────────────────────────────────────────────────────────────

    private fun applyFilters() {
        val query = binding.etSearch.text?.toString()?.lowercase()?.trim().orEmpty()

        val filtered = allDishes.filter { dish ->
            val matchSearch = query.isEmpty() ||
                    dish.itemName.lowercase().contains(query) ||
                    dish.itemCode.lowercase().contains(query)

            val categoryName = DishAdapter.CATEGORY_MAP[dish.categoryId] ?: ""
            val matchCategory = selectedCategory == null || categoryName == selectedCategory

            val statusName = if (dish.isActive) "Активно" else "Неактивно"
            val matchStatus = selectedStatus == null || statusName == selectedStatus

            matchSearch && matchCategory && matchStatus
        }

        adapter.submitList(filtered)
    }

    // ──────────────────────────────────────────────────────────────
    // Контекстное меню строки
    // ──────────────────────────────────────────────────────────────

    private fun showDishMenu(anchor: View, dish: DishResponse) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_dish_actions, popup.menu)

        // Красный цвет для пункта "Удалить"
        popup.menu.findItem(R.id.action_delete).title =
            SpannableString("Удалить").apply {
                setSpan(
                    ForegroundColorSpan(Color.parseColor("#f44336")),
                    0, length,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

        // Иконки в PopupMenu видимы с API 29+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_open -> {
                    // TODO: startActivity(Intent(this, DishDetailActivity::class.java)
                    //           .putExtra("dish_id", dish.id))
                    true
                }

                R.id.action_add_photo -> {
                    // TODO: startActivity(Intent(this, AddDishPhotoActivity::class.java)
                    //           .putExtra("dish_id", dish.id))
                    true
                }

                R.id.action_deactivate -> {
                    // TODO: PATCH /dishes/{dish.id} с { "is_active": !dish.isActive }
                    //       затем обновить список: loadDishes()
                    true
                }

                R.id.action_delete -> {
                    showDeleteConfirmDialog(dish)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmDialog(dish: DishResponse) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Удалить блюдо?")
            .setMessage("Блюдо «${dish.itemName}» (${dish.itemCode}) будет удалено безвозвратно.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                // TODO: DELETE /dishes/{dish.id}, затем loadDishes()
            }
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}