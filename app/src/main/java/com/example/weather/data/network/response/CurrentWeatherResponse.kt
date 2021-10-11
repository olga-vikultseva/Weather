package com.example.weather.data.network.response

import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("current")
    val current: Current,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("timezone")
    val location: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int
)

data class Current(
    @SerializedName("clouds")
    val clouds: Int,
    @SerializedName("dew_point")
    val dewPoint: Double,
    @SerializedName("dt")
    val timestampSec: Long,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("humidity")
    val humidity: Int,
    @SerializedName("pressure")
    val pressure: Int,
    @SerializedName("sunrise")
    val sunrise: Long,
    @SerializedName("sunset")
    val sunset: Long,
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("uvi")
    val uvI: Double,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind_deg")
    val windDeg: Int,
    @SerializedName("wind_speed")
    val windSpeed: Double
)


