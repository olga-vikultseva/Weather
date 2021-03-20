package com.example.weather.data.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.weather.data.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnitProviderImpl(
    context: Context
) : PreferenceProvider(context), UnitProvider {

    override val isMetricUnit: Boolean
        get() = getPreferredUnitSystem() == UnitSystem.METRIC

    private val _unitSystemFlow = MutableStateFlow(getPreferredUnitSystem())
    override val unitSystemFlow = _unitSystemFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PREFERRED_UNIT_SYSTEM -> {
                _unitSystemFlow.value = getPreferredUnitSystem()
            }
        }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun updateUnitSystemPreference(unitSystem: UnitSystem) = preferences.edit {
        putString(PREFERRED_UNIT_SYSTEM, unitSystem.name)
    }

    override fun persistUnitSystemForLastRequestCurrentWeather(unitSystem: UnitSystem) =
        preferences.edit {
            putString(UNIT_SYSTEM_FOR_LAST_REQUEST_CURRENT_WEATHER, unitSystem.name)
        }

    override fun persistUnitSystemForLastRequestFutureWeather(unitSystem: UnitSystem) =
        preferences.edit {
            putString(UNIT_SYSTEM_FOR_LAST_REQUEST_FUTURE_WEATHER, unitSystem.name)
        }

    override fun getUnitSystemForLastRequestCurrentWeather(): UnitSystem? =
        preferences.getString(UNIT_SYSTEM_FOR_LAST_REQUEST_CURRENT_WEATHER, null)?.let {
            UnitSystem.valueOf(it)
        }

    override fun getUnitSystemForLastRequestFutureWeather(): UnitSystem? =
        preferences.getString(UNIT_SYSTEM_FOR_LAST_REQUEST_FUTURE_WEATHER, null)?.let {
            UnitSystem.valueOf(it)
        }

    private fun getPreferredUnitSystem(): UnitSystem =
        UnitSystem.valueOf(preferences.getString(PREFERRED_UNIT_SYSTEM, UnitSystem.METRIC.name)!!)

    private companion object {
        const val PREFERRED_UNIT_SYSTEM = "PREFERRED_UNIT_SYSTEM"
        const val UNIT_SYSTEM_FOR_LAST_REQUEST_CURRENT_WEATHER = "UNIT_SYSTEM_FOR_LAST_REQUEST_CURRENT_WEATHER"
        const val UNIT_SYSTEM_FOR_LAST_REQUEST_FUTURE_WEATHER = "UNIT_SYSTEM_FOR_LAST_REQUEST_FUTURE_WEATHER"
    }
}