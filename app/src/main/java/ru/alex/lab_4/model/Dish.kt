package ru.alex.lab_4.model

import com.google.gson.annotations.SerializedName

data class DishResponse(
    @SerializedName("id")               val id: Int,
    @SerializedName("item_code")        val itemCode: String,
    @SerializedName("item_name")        val itemName: String,
    @SerializedName("category_id")      val categoryId: Int,
    @SerializedName("is_active")        val isActive: Boolean,
    @SerializedName("created_at")       val createdAt: String,
    @SerializedName("updated_at")       val updatedAt: String,
    @SerializedName("reference_images") val referenceImages: List<ReferenceImage>
)

data class ReferenceImage(
    @SerializedName("id")         val id: Int,
    @SerializedName("dish_id")    val dishId: Int,
    @SerializedName("image_path") val imagePath: String,
    @SerializedName("image_hash") val imageHash: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("image_url")  val imageUrl: String
)