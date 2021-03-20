package com.example.weather.domain.settings

import android.Manifest
import android.content.pm.PackageManager
import com.example.weather.data.ResultWrapper
import com.example.weather.data.UnitSystem
import com.example.weather.data.providers.LocationProvider
import com.example.weather.data.providers.UnitProvider
import com.example.weather.domain.settings.model.LocationSettingState
import com.example.weather.domain.settings.model.UnitSystemSettingState
import com.example.weather.domain.settings.model.UseDeviceLocationSettingState
import com.example.weather.internal.MutableUpdateStateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Closeable

class SettingsInteractorImpl(
    private val locationProvider: LocationProvider,
    private val unitProvider: UnitProvider
) : SettingsInteractor, Closeable {

    private val _useDeviceLocationSettingFlow = MutableUpdateStateFlow<UseDeviceLocationSettingState>(UseDeviceLocationSettingState.Loading)
    override val useDeviceLocationSettingFlow = _useDeviceLocationSettingFlow.asStateFlow()

    private val _locationSettingFlow = MutableStateFlow<LocationSettingState>(LocationSettingState.Loading)
    override val locationSettingFlow = _locationSettingFlow.asStateFlow()

    private val _unitSystemSettingFlow = MutableStateFlow<UnitSystemSettingState>(UnitSystemSettingState.Loading)
    override val unitSystemSettingFlow = _unitSystemSettingFlow.asStateFlow()

    private lateinit var useDeviceLocationSettingUpdatesJob: Job
    private lateinit var locationSettingUpdatesJob: Job
    private lateinit var unitSystemSettingUpdatesJob: Job

    private lateinit var locationUpdateJob: Job

    private val _permissionRequestTrigger = MutableUpdateStateFlow<Pair<String?, Int?>>(null to null)
    override val permissionRequestTrigger = _permissionRequestTrigger.asStateFlow()

    init {
        subscribeToUseDeviceLocationSettingUpdates()
        subscribeToLocationSettingUpdates()
        subscribeToUnitSystemSettingUpdates()
    }

    override fun requestLocationUpdate() {
        if (!this::locationUpdateJob.isInitialized || locationUpdateJob.isCompleted) {
            locationUpdateJob = GlobalScope.launch {
                locationProvider.requestLocationUpdate()
            }
        }
    }

    override fun onUseDeviceLocationCheckedChange() {
        val isChecked = !locationProvider.isUsingDeviceLocation()
        if (isChecked) {
            _permissionRequestTrigger.value = Manifest.permission.ACCESS_FINE_LOCATION to LOCATION_PERMISSION_REQUEST
        } else {
            locationProvider.updateUseDeviceLocationPreference(false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                locationProvider.updateUseDeviceLocationPreference(true)
            } else {
                _useDeviceLocationSettingFlow.value = UseDeviceLocationSettingState.Disabled
            }
        }
    }

    override fun onSelectionUnitSystemResult(unitSystem: UnitSystem) {
        unitProvider.updateUnitSystemPreference(unitSystem)
    }

    private fun subscribeToUseDeviceLocationSettingUpdates() {
        useDeviceLocationSettingUpdatesJob = GlobalScope.launch {
            locationProvider.useDeviceLocationFlow
                .map { useDeviceLocation ->
                    when (useDeviceLocation) {
                        true -> UseDeviceLocationSettingState.Enabled
                        false -> UseDeviceLocationSettingState.Disabled
                    }
                }
                .collect {
                    _useDeviceLocationSettingFlow.value = it
                }
        }
    }

    private fun subscribeToLocationSettingUpdates() {
        locationSettingUpdatesJob = GlobalScope.launch {
            locationProvider.locationFlow.combine(locationProvider.useDeviceLocationFlow) { locationResult, useDeviceLocation -> locationResult to useDeviceLocation }
                .map { (locationResult, useDeviceLocation) ->
                    when (locationResult) {
                        is ResultWrapper.Loading -> LocationSettingState.Loading
                        is ResultWrapper.Success -> LocationSettingState.Data(
                            location = locationResult.data,
                            isClickable = !useDeviceLocation
                        )
                        is ResultWrapper.Error -> LocationSettingState.Error(error = locationResult.error)
                    }
                }
                .collect {
                    _locationSettingFlow.value = it
                }
        }
    }

    private fun subscribeToUnitSystemSettingUpdates() {
        unitSystemSettingUpdatesJob = GlobalScope.launch {
            unitProvider.unitSystemFlow
                .map { unitSystem ->
                    when (unitSystem) {
                        UnitSystem.METRIC -> UnitSystemSettingState.Metric
                        UnitSystem.IMPERIAL -> UnitSystemSettingState.Imperial
                    }
                }
                .collect {
                    _unitSystemSettingFlow.value = it
                }
        }
    }

    override fun close() {
        useDeviceLocationSettingUpdatesJob.cancel()
        locationSettingUpdatesJob.cancel()
        unitSystemSettingUpdatesJob.cancel()
        if (this::locationUpdateJob.isInitialized && locationUpdateJob.isActive) {
            locationUpdateJob.cancel()
        }
    }

    private companion object {
        const val LOCATION_PERMISSION_REQUEST = 100
    }
}