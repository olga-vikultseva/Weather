package com.example.weather.domain.settings.model

sealed class UnitSystemSettingState {
    object Loading : UnitSystemSettingState()
    object Metric : UnitSystemSettingState()
    object Imperial : UnitSystemSettingState()
}
