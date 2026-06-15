package ru.alex.lab_4.api

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import ru.taf.lab_4.ui.activities.LoginActivity
import ru.taf.lab_4.model.App
import java.util.Base64
import kotlin.jvm.java

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = prefs.getString("access_token", null)

        // Проверка локально: не истек ли токен (exp)
        if (token != null && isTokenExpired(token)) {
            redirectToLogin()
            // Возвращаем пустой ответ или кидаем ошибку, но лучше прервать цепочку
            return chain.proceed(chain.request()) 
        }

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // Если сервер вернул 401 - разлогиниваем
        if (response.code == 401) {
            redirectToLogin()
        }

        return response
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val json = JSONObject(payload)
            val exp = json.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000
            exp != 0L && exp < currentTime
        } catch (e: Exception) {
            false
        }
    }

    private fun redirectToLogin() {
        // Очищаем префы
        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
        
        // Переходим на логин (нужен Handler, так как мы в фоновом потоке OkHttp)
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
