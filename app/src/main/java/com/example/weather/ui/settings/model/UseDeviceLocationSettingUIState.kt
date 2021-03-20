package com.example.weather.ui.settings.model

sealed class UseDeviceLocationSettingUIState {
    object Loading : UseDeviceLocationSettingUIState()
    object Enabled : UseDeviceLocationSettingUIState()
    object Disabled : UseDeviceLocationSettingUIState()
}