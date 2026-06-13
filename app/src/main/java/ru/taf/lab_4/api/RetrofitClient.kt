package ru.taf.lab_4.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.taf.lab_4.api.AuthApi
import ru.taf.lab_4.model.App

object RetrofitClient {
    private const val BASE_URL = "http://10.8.0.2:8000/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val prefs = App.instance.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)
        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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
}