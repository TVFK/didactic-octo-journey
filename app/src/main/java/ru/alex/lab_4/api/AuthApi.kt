package ru.taf.lab_4.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.taf.lab_4.model.LoginRequest
import ru.taf.lab_4.model.LoginResponse
import ru.taf.lab_4.model.ModuleStatusResponse

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}