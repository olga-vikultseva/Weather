package com.example.weather.data.providers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.weather.data.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnitSystemProviderImpl(
    context: Context
) : PreferenceProvider(context), UnitSystemProvider {

    override val isMetricUnitSystem: Boolean
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

    override fun updateUnitSystem(unitSystem: UnitSystem) {
        preferences.edit {
            putString(PREFERRED_UNIT_SYSTEM, unitSystem.name)
        }
    }

    private fun getPreferredUnitSystem(): UnitSystem =
        UnitSystem.valueOf(preferences.getString(PREFERRED_UNIT_SYSTEM, UnitSystem.METRIC.name)!!)

    companion object {
        private const val PREFERRED_UNIT_SYSTEM = "PREFERRED_UNIT_SYSTEM"
    }
}