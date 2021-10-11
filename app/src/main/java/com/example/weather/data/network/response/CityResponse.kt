package com.example.weather.data.network.response

import com.google.gson.annotations.SerializedName

data class CityResponse(
    @SerializedName("results")
    val results: List<Result>,
    @SerializedName("status")
    val status: Status
)

data class Result(
    @SerializedName("components")
    val components: Components,
    @SerializedName("formatted")
    val formatted: String,
    @SerializedName("geometry")
    val geometry: Geometry
)

data class Components(
    @SerializedName("city")
    val city: String?,
    @SerializedName("city_district")
    val cityDistrict: String?,
    @SerializedName("county")
    val county: String?
)

data class Geometry(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class Status(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)
