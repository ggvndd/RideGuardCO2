package com.capstoneco2.rideguard.data.models

import com.google.gson.annotations.SerializedName

/**
 * Data class for sending accident reports to the API
 */
data class AccidentReportRequest(
    @SerializedName("phone_number")
    val phoneNumber: String
)

/**
 * Location data for the accident report
 */
data class LocationData(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("address")
    val address: String
)

/**
 * Vehicle information for the accident report
 */
data class VehicleInfo(
    @SerializedName("make")
    val make: String,
    @SerializedName("model")
    val model: String,
    @SerializedName("year")
    val year: Int,
    @SerializedName("license_plate")
    val licensePlate: String
)

/**
 * Response from the API after submitting an accident report
 */
data class AccidentReportResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("report_id")
    val reportId: String?,
    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * General API error response
 */
data class ApiError(
    @SerializedName("error")
    val error: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int
)