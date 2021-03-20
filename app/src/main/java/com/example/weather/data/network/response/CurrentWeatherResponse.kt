package com.example.weather.data.network.response

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    val current: Current,
    val lat: Double,
    val lon: Double,
    @SerializedName("timezone")
    val location: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int
)

data class Current(
    val clouds: Int,
    @SerializedName("dew_point")
    val dewPoint: Double,
    @SerializedName("dt")
    val timestampSec: Long,
    @SerializedName("feels_like")
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Int,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    @SerializedName("uvi")
    val uvI: Double,
    val visibility: Int,
    val weather: List<Weather>,
    @SerializedName("wind_deg")
    val windDeg: Int,
    @SerializedName("wind_speed")
    val windSpeed: Double
)


