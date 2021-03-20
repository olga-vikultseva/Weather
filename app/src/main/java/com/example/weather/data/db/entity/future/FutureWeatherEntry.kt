package com.example.weather.data.db.entity.future

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "future_weather")
data class FutureWeatherEntry(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "day_of_week_int")
    val dayOfWeekInt: Int,
    @ColumnInfo(name = "timestamp_millis")
    val timestampMillis: Long,
    @Embedded
    val day: Day
)

data class Day(
    @ColumnInfo(name = "temp_max")
    val tempMax: Int,
    @ColumnInfo(name = "temp_min")
    val tempMin: Int,
    @ColumnInfo(name = "temp_morn")
    val tempMorn: Int,
    @ColumnInfo(name = "temp_day")
    val tempDay: Int,
    @ColumnInfo(name = "temp_eve")
    val tempEve: Int,
    @ColumnInfo(name = "temp_night")
    val tempNight: Int,
    @ColumnInfo(name = "feels_like_morn")
    val feelsLikeMorn: Int,
    @ColumnInfo(name = "feels_like_day")
    val feelsLikeDay: Int,
    @ColumnInfo(name = "feels_like_eve")
    val feelsLikeEve: Int,
    @ColumnInfo(name = "feels_like_night")
    val feelsLikeNight: Int,
    @ColumnInfo(name = "wind_direction")
    val windDirection: String,
    @ColumnInfo(name = "wind_speed")
    val windSpeed: Int,
    val pressure: Int,
    val humidity: Int,
    @ColumnInfo(name = "uv_index")
    val uVIndex: Double,
    @ColumnInfo(name = "weather_description")
    val weatherDescription: String,
    @ColumnInfo(name = "weather_icon_url")
    val weatherIconUrl: String
)