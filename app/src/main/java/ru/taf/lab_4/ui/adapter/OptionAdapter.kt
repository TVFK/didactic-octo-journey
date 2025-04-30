package ru.taf.lab_4.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.taf.lab_4.R
import ru.taf.lab_4.model.OptionItem

class OptionAdapter(
    private val options: List<OptionItem>,
    private val onOptionSelected: (isCorrect: Boolean) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    private var selectedPosition = -1
    private var isAnswerRevealed = false
    private var correctPosition = -1

    init {
        correctPosition = options.indexOfFirst { it.correct }
    }

    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val number: TextView = itemView.findViewById(R.id.optionNumber)
        val text: TextView = itemView.findViewById(R.id.optionText)
        val container: LinearLayout = itemView.findViewById(R.id.optionContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]

        holder.number.text = (position + 1).toString()
        holder.text.text = option.word

        resetOptionAppearance(holder)

        if (isAnswerRevealed) {
            when {
                option.correct -> setCorrectOptionAppearance(holder)
                position == selectedPosition -> setWrongOptionAppearance(holder)
            }
        } else {
            if (position == selectedPosition) {
                holder.container.setBackgroundResource(R.drawable.shape_rounded_option_selected)
            }
        }

        holder.container.setOnClickListener {
            if (!isAnswerRevealed) {
                selectedPosition = holder.adapterPosition
                isAnswerRevealed = true
                notifyItemChanged(selectedPosition)
                if (correctPosition != -1) notifyItemChanged(correctPosition)
                onOptionSelected(option.correct)
            }
        }
    }

    override fun getItemCount() = options.size

    private fun resetOptionAppearance(holder: OptionViewHolder) {
        holder.container.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option
        )
        holder.number.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option_number
        )
        holder.number.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_text))
        holder.text.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.gray_text))
    }

    private fun setCorrectOptionAppearance(holder: OptionViewHolder) {
        holder.container.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option_correct_stroke
        )
        holder.number.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option_number_correct
        )
        holder.number.setTextColor(Color.WHITE)
        holder.text.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
    }

    private fun setWrongOptionAppearance(holder: OptionViewHolder) {
        holder.container.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option_wrong_stroke
        )
        holder.number.background = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.shape_rounded_option_number_wrong
        )
        holder.number.setTextColor(Color.WHITE)
        holder.text.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
    }
}