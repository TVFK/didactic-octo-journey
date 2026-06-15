package ru.alex.lab_4.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.alex.lab_4.model.DishResponse
import ru.taf.lab_4.R
import ru.taf.lab_4.databinding.ItemDishRowBinding

class DishAdapter(
    private val onMenuClick: (anchor: View, dish: DishResponse) -> Unit
) : ListAdapter<DishResponse, DishAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemDishRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DiffCallback : DiffUtil.ItemCallback<DishResponse>() {
        override fun areItemsTheSame(old: DishResponse, new: DishResponse) = old.id == new.id
        override fun areContentsTheSame(old: DishResponse, new: DishResponse) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDishRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = getItem(position)
        with(holder.binding) {
            // Фото: первое эталонное изображение или заглушка
            Glide.with(holder.itemView)
                .load(dish.referenceImages.firstOrNull()?.imageUrl)
                .placeholder(R.drawable.bg_dish_photo)
                .error(R.drawable.bg_dish_photo)
                .centerCrop()
                .into(ivDishPhoto)

            tvDishName.text     = dish.itemName
            tvDishCode.text     = dish.itemCode
            tvDishCategory.text = CATEGORY_MAP[dish.categoryId] ?: "Категория ${dish.categoryId}"
            tvPhotoCount.text   = dish.referenceImages.size.toString()
            tvUpdatedDate.text  = formatDate(dish.updatedAt)

            // Статус-чип
            val isActive = dish.isActive
            chipDishStatus.apply {
                text = if (isActive) "Активно" else "Неактивно"
                setTextColor(Color.parseColor(if (isActive) "#4caf50" else "#666666"))
                chipBackgroundColor = ColorStateList.valueOf(
                    Color.parseColor(if (isActive) "#e8f5e9" else "#f5f5f5")
                )
            }

            btnDishMenu.setOnClickListener { onMenuClick(it, dish) }
        }
    }

    // "2026-06-14T16:55:27.600661Z" → "14.06.2026"
    // Используем substring, т.к. SimpleDateFormat не поддерживает микросекунды (6 знаков)
    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val (y, m, d) = isoDate.take(10).split("-")
            "$d.$m.$y"
        } catch (e: Exception) {
            isoDate
        }
    }

    companion object {
        // TODO: заменить на запрос к /categories/ при наличии такой ручки
        val CATEGORY_MAP = mapOf(
            1 to "Салаты",
            2 to "Супы",
            3 to "Основное",
            4 to "Гарниры",
            5 to "Десерты"
        )
    }
}