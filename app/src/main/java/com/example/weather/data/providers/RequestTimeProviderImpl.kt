package com.example.weather.data.providers

import android.content.Context
import androidx.core.content.edit

class RequestTimeProviderImpl(
    context: Context
) : PreferenceProvider(context), RequestTimeProvider {

    override fun persistTimeForLastRequestCurrentWeather(timestampMillis: Long) = preferences.edit {
        putLong(TIME_FOR_LAST_REQUEST_CURRENT_WEATHER, timestampMillis)
    }

    override fun persistTimeForLastRequestFutureWeather(timestampMillis: Long) = preferences.edit {
        putLong(TIME_FOR_LAST_REQUEST_FUTURE_WEATHER, timestampMillis)
    }

    override fun getTimestampMillisForLastRequestCurrentWeather(): Long =
        preferences.getLong(TIME_FOR_LAST_REQUEST_CURRENT_WEATHER, 0L)

    override fun getTimestampMillisForLastRequestFutureWeather(): Long =
        preferences.getLong(TIME_FOR_LAST_REQUEST_FUTURE_WEATHER, 0L)

    private companion object {
        const val TIME_FOR_LAST_REQUEST_CURRENT_WEATHER = "TIME_FOR_LAST_REQUEST_CURRENT_WEATHER"
        const val TIME_FOR_LAST_REQUEST_FUTURE_WEATHER = "TIME_FOR_LAST_REQUEST_FUTURE_WEATHER"
    }
}