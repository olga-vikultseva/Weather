package com.example.weather.data.providers

interface RequestTimeProvider {
    fun persistTimeForLastRequestCurrentWeather(timestampMillis: Long)
    fun persistTimeForLastRequestFutureWeather(timestampMillis: Long)
    fun getTimestampMillisForLastRequestCurrentWeather(): Long
    fun getTimestampMillisForLastRequestFutureWeather(): Long
}