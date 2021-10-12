package com.example.weather.data.repositories

import android.content.Context
import androidx.core.content.edit
import com.example.weather.data.UnitSystem
import com.example.weather.data.WeatherLocation
import com.example.weather.data.providers.PreferenceProvider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LastWeatherRequestParamsProvider(
    context: Context
) : PreferenceProvider(context) {

    var currentWeatherLocation: WeatherLocation?
        get() = preferences.getString(CURRENT_WEATHER_LOCATION, null)?.let {
            Json.decodeFromString(it)
        }
        set(value) {
            preferences.edit {
                putString(CURRENT_WEATHER_LOCATION, Json.encodeToString(value))
            }
        }

    var futureWeatherLocation: WeatherLocation?
        get() = preferences.getString(FUTURE_WEATHER_LOCATION, null)?.let {
            Json.decodeFromString(it)
        }
        set(value) {
            preferences.edit {
                putString(FUTURE_WEATHER_LOCATION, Json.encodeToString(value))
            }
        }

    var currentWeatherUnitSystem: UnitSystem?
        get() = preferences.getString(CURRENT_WEATHER_UNIT_SYSTEM, null)?.let {
            UnitSystem.valueOf(it)
        }
        set(value) {
            preferences.edit {
                putString(CURRENT_WEATHER_UNIT_SYSTEM, value?.name)
            }
        }

    var futureWeatherUnitSystem: UnitSystem?
        get() = preferences.getString(FUTURE_WEATHER_UNIT_SYSTEM, null)?.let {
            UnitSystem.valueOf(it)
        }
        set(value) {
            preferences.edit {
                putString(FUTURE_WEATHER_UNIT_SYSTEM, value?.name)
            }
        }

    var currentWeatherRequestTimeMillis: Long
        get() = preferences.getLong(CURRENT_WEATHER_REQUEST_TIME, 0L)
        set(value) {
            preferences.edit {
                putLong(CURRENT_WEATHER_REQUEST_TIME, value)
            }
        }

    var futureWeatherRequestTimeMillis: Long
        get() = preferences.getLong(FUTURE_WEATHER_REQUEST_TIME, 0L)
        set(value) {
            preferences.edit {
                putLong(FUTURE_WEATHER_REQUEST_TIME, value)
            }
        }

    companion object {
        private const val CURRENT_WEATHER_LOCATION = "CURRENT_WEATHER_LOCATION"
        private const val FUTURE_WEATHER_LOCATION = "FUTURE_WEATHER_LOCATION"
        private const val CURRENT_WEATHER_UNIT_SYSTEM = "CURRENT_WEATHER_UNIT_SYSTEM"
        private const val FUTURE_WEATHER_UNIT_SYSTEM = "FUTURE_WEATHER_UNIT_SYSTEM"
        private const val CURRENT_WEATHER_REQUEST_TIME = "CURRENT_WEATHER_REQUEST_TIME"
        private const val FUTURE_WEATHER_REQUEST_TIME = "FUTURE_WEATHER_REQUEST_TIME"
    }
}