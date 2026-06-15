package ru.taf.lab_4.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.alex.lab_4.api.AuthInterceptor
import ru.alex.lab_4.api.DishApi
import ru.taf.lab_4.api.AuthApi
import ru.taf.lab_4.model.App
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "http://10.8.0.2:8000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(App.instance))
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        instance.create(AuthApi::class.java)
    }

    val moduleApi: ModuleApi by lazy {
        instance.create(ModuleApi::class.java)
    }

    val dishApi: DishApi by lazy { instance.create(DishApi::class.java) }
}