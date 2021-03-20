package com.example.weather.data.network.response

import com.google.gson.annotations.SerializedName

data class CityResponse(
    val results: List<Result>,
    val status: Status
)

data class Result(
    val components: Components,
    val formatted: String,
    val geometry: Geometry
)

data class Components(
    val city: String?,
    @SerializedName("city_district")
    val cityDistrict: String?,
    val county: String?
)

data class Geometry(
    val lat: Double,
    val lng: Double
)

data class Status(
    val code: Int,
    val message: String
)
