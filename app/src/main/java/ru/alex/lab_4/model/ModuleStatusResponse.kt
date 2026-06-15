package ru.taf.lab_4.model

import com.google.gson.annotations.SerializedName

data class ModuleStatusResponse(
    @SerializedName("total_modules")
    val totalModules: Int,
    @SerializedName("working_modules")
    val workingModules: Int,
    @SerializedName("not_working_modules")
    val notWorkingModules: Int,
    @SerializedName("modules_with_unavailable_camera")
    val modulesWithUnavailableCamera: Int,
    val modules: List<ModuleInfo>
)

data class ModuleInfo(
    @SerializedName("module_code")
    val moduleCode: String,
    @SerializedName("station_name")
    val stationName: String,
    val mode: String,
    val status: String,
    @SerializedName("camera_status")
    val cameraStatus: String
)