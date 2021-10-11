package com.example.weather.data.db.entity.current

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_WEATHER = 0

@Entity(tableName = "current_weather")
data class CurrentWeatherEntry(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int = CURRENT_WEATHER,
    @ColumnInfo(name = "temperature")
    val temperature: Int,
    @ColumnInfo(name = "feels_like_temperature")
    val feelsLikeTemperature: Int,
    @ColumnInfo(name = "wind_direction")
    val windDirection: String,
    @ColumnInfo(name = "wind_speed")
    val windSpeed: Int,
    @ColumnInfo(name = "pressure")
    val pressure: Int,
    @ColumnInfo(name = "humidity")
    val humidity: Int,
    @ColumnInfo(name = "uv_index")
    val uvIndex: Int,
    @ColumnInfo(name = "weather_description")
    val weatherDescription: String,
    @ColumnInfo(name = "weather_icon_url")
    val weatherIconUrl: String
)