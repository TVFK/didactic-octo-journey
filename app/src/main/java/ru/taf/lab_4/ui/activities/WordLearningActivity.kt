package ru.taf.lab_4.ui.activities

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.taf.lab_4.R
import ru.taf.lab_4.api.RetrofitClient
import ru.taf.lab_4.data.AppDatabaseHelper
import ru.taf.lab_4.databinding.ActivityLearnWordBinding
import ru.taf.lab_4.model.OptionItem
import ru.taf.lab_4.model.Question
import ru.taf.lab_4.ui.adapter.OptionAdapter

class WordLearningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLearnWordBinding
    private lateinit var adapter: OptionAdapter
    private var currentQuestion: Question? = null
    private val dbHelper by lazy { AppDatabaseHelper(this) }
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)

        setupRecyclerView()
        loadQuestion()
        setupButtons()
    }

    private fun setupRecyclerView() {
        binding.responseOptions.layoutManager = LinearLayoutManager(this)
        binding.responseOptions.setHasFixedSize(true)
    }

    private fun loadQuestion() {
        binding.footerContainer.visibility = View.GONE
        binding.skipButton.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getRandomQuestion()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        currentQuestion = response.body()
                        currentQuestion?.let { setupQuestion(it) }
                    } else {
                        showErrorState()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showErrorState()
                }
            }
        }
    }

    private fun showErrorState() {
        val errorQuestion = Question(
            word = "Ошибка загрузки",
            options = listOf(
                OptionItem(
                    word = "Попробовать снова",
                    correct = true
                )
            )
        )
        setupQuestion(errorQuestion)
    }

    private fun setupQuestion(question: Question) {
        binding.wordToLearn.text = question.word
        adapter = OptionAdapter(question.options) { isCorrect ->
            showResult(isCorrect)
        }
        binding.responseOptions.adapter = adapter
    }

    private fun showResult(isCorrect: Boolean) {
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId != -1) {
            dbHelper.addAnswer(userId, isCorrect)
        }

        binding.footerContainer.visibility = View.VISIBLE
        binding.skipButton.visibility = View.GONE

        if (isCorrect) {
            binding.footerContainer.setBackgroundColor(Color.parseColor("#00E200"))
            binding.resultIcon.setImageResource(R.drawable.ic_thumb_up)
            binding.resultText.text = "Correct!"
        } else {
            binding.footerContainer.setBackgroundColor(Color.parseColor("#FF0000"))
            binding.resultIcon.setImageResource(R.drawable.ic_thumb_down)
            binding.resultText.text = "Wrong!"
        }
    }

    private fun setupButtons() {
        binding.btnClose.setOnClickListener { finish() }

        binding.nextButton.setOnClickListener {
            binding.footerContainer.visibility = View.GONE
            loadQuestion()
        }

        binding.skipButton.setOnClickListener {
            loadQuestion()
        }

        binding.footerContainer.setOnClickListener {
            binding.footerContainer.visibility = View.GONE
            loadQuestion()
        }
    }

    companion object {
        fun String.toColorInt(): Int = Color.parseColor(this)
    }
}