package com.example.weather.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.UnitSystem
import com.example.weather.domain.settings.SettingsInteractor
import com.example.weather.domain.settings.model.LocationSettingState
import com.example.weather.domain.settings.model.UnitSystemSettingState
import com.example.weather.domain.settings.model.UseDeviceLocationSettingState
import com.example.weather.internal.ErrorFormatter
import com.example.weather.ui.settings.model.LocationSettingUIState
import com.example.weather.ui.settings.model.UnitSystemSettingUIState
import com.example.weather.ui.settings.model.UseDeviceLocationSettingUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsInteractor: SettingsInteractor,
    private val errorFormatter: ErrorFormatter
) : ViewModel() {

    private val _useDeviceLocationSettingState = MutableLiveData<UseDeviceLocationSettingUIState>()
    val useDeviceLocationSetting: LiveData<UseDeviceLocationSettingUIState>
        get() = _useDeviceLocationSettingState

    private val _locationSettingState = MutableLiveData<LocationSettingUIState>()
    val locationSetting: LiveData<LocationSettingUIState>
        get() = _locationSettingState

    private val _unitSystemSettingState = MutableLiveData<UnitSystemSettingUIState>()
    val unitSystemSetting: LiveData<UnitSystemSettingUIState>
        get() = _unitSystemSettingState

    private val _permissionRequestTrigger = MutableLiveData<Pair<String?, Int?>>()
    val permissionRequestTrigger: LiveData<Pair<String?, Int?>>
        get() = _permissionRequestTrigger

    init {
        subscribeToUseDeviceLocationSettingUpdates()
        subscribeToLocationSettingUpdates()
        subscribeToUnitSystemSettingUpdates()
        observeToPermissionRequestTrigger()
    }

    fun requestLocationUpdate() {
        settingsInteractor.requestLocationUpdate()
    }

    private fun subscribeToUseDeviceLocationSettingUpdates() {
        viewModelScope.launch {
            settingsInteractor.useDeviceLocationSettingFlow
                .map {
                    when (it) {
                        UseDeviceLocationSettingState.Loading -> UseDeviceLocationSettingUIState.Loading
                        UseDeviceLocationSettingState.Enabled -> UseDeviceLocationSettingUIState.Enabled
                        UseDeviceLocationSettingState.Disabled -> UseDeviceLocationSettingUIState.Disabled
                    }
                }
                .collect {
                    _useDeviceLocationSettingState.value = it
                }

        }
    }

    private fun subscribeToLocationSettingUpdates() {
        viewModelScope.launch {
            settingsInteractor.locationSettingFlow
                .map {
                    when (it) {
                        is LocationSettingState.Loading -> LocationSettingUIState.Loading
                        is LocationSettingState.Data -> LocationSettingUIState.Data(
                            cityName = it.location.cityName,
                            isClickable = it.isClickable
                        )
                        is LocationSettingState.Error -> LocationSettingUIState.Error(
                            errorMessage = errorFormatter.getErrorMessage(it.error)
                        )
                    }
                }
                .collect {
                    _locationSettingState.value = it
                }
        }
    }

    private fun subscribeToUnitSystemSettingUpdates() {
        viewModelScope.launch {
            settingsInteractor.unitSystemSettingFlow
                .map {
                    when (it) {
                        UnitSystemSettingState.Loading -> UnitSystemSettingUIState.Loading
                        UnitSystemSettingState.Metric -> UnitSystemSettingUIState.Metric
                        UnitSystemSettingState.Imperial -> UnitSystemSettingUIState.Imperial
                    }
                }
                .collect {
                    _unitSystemSettingState.value = it
                }
        }
    }

    private fun observeToPermissionRequestTrigger() {
        viewModelScope.launch {
            settingsInteractor.permissionRequestTrigger
                .collect { (permission, requestCode) ->
                    if (permission != null && requestCode != null) {
                        _permissionRequestTrigger.value = permission to requestCode
                    }
                }
        }
    }

    fun onUseDeviceLocationCheckedChange() {
        settingsInteractor.onUseDeviceLocationCheckedChange()
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        _permissionRequestTrigger.value = null to null
        settingsInteractor.onRequestPermissionsResult(requestCode, grantResults)
    }

    fun onSelectionUnitSystemResult(unitSystem: UnitSystem) {
        settingsInteractor.onSelectionUnitSystemResult(unitSystem)
    }

    override fun onCleared() {
        super.onCleared()
        settingsInteractor.close()
    }
}
