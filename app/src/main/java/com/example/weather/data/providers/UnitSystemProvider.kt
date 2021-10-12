package com.example.weather.data.providers

import com.example.weather.data.UnitSystem
import kotlinx.coroutines.flow.Flow

interface UnitSystemProvider {
    val isMetricUnitSystem: Boolean
    val unitSystemFlow: Flow<UnitSystem>
    fun updateUnitSystem(unitSystem: UnitSystem)
}