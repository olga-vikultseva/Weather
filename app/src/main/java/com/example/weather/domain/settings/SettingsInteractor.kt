package com.example.weather.domain.settings

import com.example.weather.data.UnitSystem
import com.example.weather.domain.settings.model.LocationSettingState
import com.example.weather.domain.settings.model.UnitSystemSettingState
import com.example.weather.domain.settings.model.UseDeviceLocationSettingState
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {
    val useDeviceLocationSettingFlow: Flow<UseDeviceLocationSettingState>
    val locationSettingFlow: Flow<LocationSettingState>
    val unitSystemSettingFlow: Flow<UnitSystemSettingState>
    val permissionRequestTrigger: Flow<Pair<String?, Int?>>
    fun requestLocationUpdate()
    fun onUseDeviceLocationCheckedChange()
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray)
    fun onSelectionUnitSystemResult(unitSystem: UnitSystem)
    fun close()
}