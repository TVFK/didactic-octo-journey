package ru.taf.lab_4.api

import retrofit2.Response
import retrofit2.http.GET
import ru.taf.lab_4.model.ModuleStatusResponse

interface ModuleApi {
    @GET("recognition-modules/monitoring")
    suspend fun getModulesStatus(): Response<ModuleStatusResponse>
}