package com.example.weather.domain.settings.model

sealed class UseDeviceLocationSettingState {
    object Loading : UseDeviceLocationSettingState()
    object Enabled : UseDeviceLocationSettingState()
    object Disabled : UseDeviceLocationSettingState()
}
