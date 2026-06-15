package ru.alex.lab_4.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.alex.lab_4.model.DishResponse

interface DishApi {
    @GET("dishes/")
    suspend fun getDishes(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<DishResponse>>

    // TODO: добавить эндпоинты по мере появления:
    // @PATCH("dishes/{id}") suspend fun deactivateDish(@Path("id") id: Int, ...): Response<DishResponse>
    // @DELETE("dishes/{id}") suspend fun deleteDish(@Path("id") id: Int): Response<Unit>
}