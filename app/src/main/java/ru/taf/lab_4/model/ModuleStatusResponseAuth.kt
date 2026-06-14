package ru.taf.lab_4.model

import com.google.gson.annotations.SerializedName

data class ModuleStatusResponseAuth(
    @SerializedName("total_modules")                   val totalModules: Int,
    @SerializedName("working_modules")                 val workingModules: Int,
    @SerializedName("not_working_modules")             val notWorkingModules: Int,
    @SerializedName("modules_with_unavailable_camera") val modulesWithUnavailableCamera: Int,
    val modules: List<ModuleSummary>
)

data class ModuleSummary(
    @SerializedName("module_id")     val moduleId: Int,
    @SerializedName("module_code")   val moduleCode: String,
    @SerializedName("station_name")  val stationName: String,
    val mode: String,
    val status: String,
    @SerializedName("camera_status") val cameraStatus: String,
    val url: String,
    @SerializedName("last_seen_at")  val lastSeenAt: String,
    @SerializedName("model_version") val modelVersion: String
)

// Временный синоним — когда будет известна точная схема ответа
// GET /recognition-modules/{id}, вынести в отдельный data class
typealias ModuleDetail = ModuleSummary