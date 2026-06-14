package ru.taf.lab_4.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.taf.lab_4.model.ModuleDetail
import ru.taf.lab_4.model.ModuleStatusResponse
import ru.taf.lab_4.model.ModuleStatusResponseAuth

interface ModuleApi {
    @GET("recognition-modules/monitoring")
    suspend fun getModulesStatus(): Response<ModuleStatusResponse>

    @GET("recognition-modules/monitoring/auth")
    suspend fun getModulesStatusAuth(): Response<ModuleStatusResponseAuth>

    @GET("recognition-modules/{module_id}")
    suspend fun getModuleDetail(
        @Path("module_id") moduleId: Int
    ): Response<ModuleDetail>
}