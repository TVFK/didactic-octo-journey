package ru.taf.lab_4.model

data class Question(
    val word: String,
    val options: List<OptionItem>
)