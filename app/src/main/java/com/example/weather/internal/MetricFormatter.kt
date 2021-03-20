package com.example.weather.internal

interface MetricFormatter {
    fun getFormattedTemperature(temperature: Int): String
    fun getFormattedWindSpeed(windSpeed: Int): String
}