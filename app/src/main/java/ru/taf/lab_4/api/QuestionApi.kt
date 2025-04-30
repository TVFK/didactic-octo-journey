package ru.taf.lab_4.api

import retrofit2.Response
import retrofit2.http.GET
import ru.taf.lab_4.model.Question

interface QuestionApi {

    @GET("words/random-question")
    suspend fun getRandomQuestion() : Response<Question>
}