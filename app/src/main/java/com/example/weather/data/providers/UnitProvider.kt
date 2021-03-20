package com.example.weather.data.providers

import com.example.weather.data.UnitSystem
import kotlinx.coroutines.flow.Flow

interface UnitProvider {
    val isMetricUnit: Boolean
    val unitSystemFlow: Flow<UnitSystem>
    fun updateUnitSystemPreference(unitSystem: UnitSystem)
    fun persistUnitSystemForLastRequestCurrentWeather(unitSystem: UnitSystem)
    fun persistUnitSystemForLastRequestFutureWeather(unitSystem: UnitSystem)
    fun getUnitSystemForLastRequestCurrentWeather(): UnitSystem?
    fun getUnitSystemForLastRequestFutureWeather():UnitSystem?
}