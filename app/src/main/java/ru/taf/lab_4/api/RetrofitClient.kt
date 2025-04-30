package ru.taf.lab_4.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "http://192.168.1.69:8080/api/v1/"

    private const val BASE_URL = "http://192.168.77.39:8080/api/v1/"

    val instance: QuestionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuestionApi::class.java)
    }
}