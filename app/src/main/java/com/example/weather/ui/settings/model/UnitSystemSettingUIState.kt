package com.example.weather.ui.settings.model

sealed class UnitSystemSettingUIState {
    object Loading : UnitSystemSettingUIState()
    object Metric : UnitSystemSettingUIState()
    object Imperial : UnitSystemSettingUIState()
}
