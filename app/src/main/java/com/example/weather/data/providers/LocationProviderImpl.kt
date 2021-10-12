package com.example.weather.data.providers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.location.component1
import androidx.core.location.component2
import com.example.weather.data.ResultWrapper
import com.example.weather.data.WeatherLocation
import com.example.weather.data.datasources.CityDataSource
import com.example.weather.data.network.response.CityResponse
import com.example.weather.internal.asDeferred
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocationProviderImpl(
    private val context: Context,
    private val cityDataSource: CityDataSource,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : PreferenceProvider(context), LocationProvider {

    override val isUseDeviceLocation: Boolean
        get() = preferences.getBoolean(USE_DEVICE_LOCATION_PREFERENCE, false)

    private val _useDeviceLocationFlow = MutableStateFlow(isUseDeviceLocation)
    override val useDeviceLocationFlow = _useDeviceLocationFlow.asStateFlow()

    private val _locationFlow = MutableStateFlow<ResultWrapper<WeatherLocation>>(ResultWrapper.Loading())
    override val locationFlow = _locationFlow.asStateFlow()

    private lateinit var locationCallback: LocationCallback

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            USE_DEVICE_LOCATION_PREFERENCE -> {
                _locationFlow.value = ResultWrapper.Loading()
                if (isUseDeviceLocation) {
                    _useDeviceLocationFlow.value = true
                    startLocationUpdates()
                } else {
                    _useDeviceLocationFlow.value = false
                    stopLocationUpdate()
                }
                GlobalScope.launch {
                    _locationFlow.value = getLocation()
                }
            }
            PREFERRED_LOCATION -> {
                _locationFlow.value = ResultWrapper.Success(getPreferredLocation())
            }
        }
    }

    init {
        if (isUseDeviceLocation) {
            _useDeviceLocationFlow.value = true
            startLocationUpdates()
        } else {
            _useDeviceLocationFlow.value = false
        }
        GlobalScope.launch {
            _locationFlow.value = getLocation()
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun updateUseDeviceLocation(value: Boolean) {
        preferences.edit {
            putBoolean(USE_DEVICE_LOCATION_PREFERENCE, value)
        }
    }

    override fun updateLocation(location: WeatherLocation) {
        preferences.edit {
            putString(PREFERRED_LOCATION, Json.encodeToString(location))
        }
    }

    override suspend fun requestLocationUpdate() {
        _locationFlow.value = ResultWrapper.Loading()
        _locationFlow.value = getLocation()
    }

    private fun getPreferredLocation(): WeatherLocation =
        preferences.getString(PREFERRED_LOCATION, null)?.let {
            Json.decodeFromString(it)
        } ?: defaultLocation

    private suspend fun getLocation(): ResultWrapper<WeatherLocation> =
        if (isUseDeviceLocation) {
            if (hasLocationPermissionGranted()) {
                getDeviceLocationCoordinatesAsync().await()?.let { currentDeviceLocation ->
                    if (isDeviceLocationChanged(
                            currentLatitude = currentDeviceLocation.latitude,
                            currentLongitude = currentDeviceLocation.longitude
                        )
                    ) {
                        cityDataSource.searchCity(
                            latitude = currentDeviceLocation.latitude,
                            longitude = currentDeviceLocation.longitude
                        ).let { cityQueryResult ->
                            when (cityQueryResult) {
                                is ResultWrapper.Success -> {
                                    val cityName = fetchCityName(cityQueryResult.data)
                                    updateLocation(
                                        WeatherLocation(
                                            cityName = cityName,
                                            latitude = currentDeviceLocation.latitude,
                                            longitude = currentDeviceLocation.longitude
                                        )
                                    )
                                    ResultWrapper.Success(getPreferredLocation())
                                }
                                is ResultWrapper.Loading -> ResultWrapper.Loading()
                                is ResultWrapper.Error -> ResultWrapper.Error(cityQueryResult.error)
                            }
                        }
                    } else {
                        ResultWrapper.Success(getPreferredLocation())
                    }
                } ?: ResultWrapper.Loading()
            } else {
                updateUseDeviceLocation(false)
                ResultWrapper.Success(getPreferredLocation())
            }
        } else {
            ResultWrapper.Success(getPreferredLocation())
        }

    private fun fetchCityName(response: CityResponse): String =
        response.results.firstOrNull()?.let { result ->
            result.components.city ?: result.components.cityDistrict ?: result.components.county ?: result.formatted
        } ?: "Unknown city"

    @SuppressLint("MissingPermission")
    private fun getDeviceLocationCoordinatesAsync(): Deferred<Location?> =
        fusedLocationProviderClient.lastLocation.asDeferred()

    private fun hasLocationPermissionGranted(): Boolean =
        (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            interval = LOCATION_UPDATE_INTERVAL_MILLIS
            smallestDisplacement = SMALLEST_DISPLACEMENT_METERS
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null && locationResult.locations.isNotEmpty()) {
                    val (latitude, longitude) = locationResult.locations.first()
                    GlobalScope.launch {
                        cityDataSource.searchCity(latitude, longitude).let { cityResult ->
                            when (cityResult) {
                                is ResultWrapper.Success -> {
                                    WeatherLocation(
                                        latitude = latitude,
                                        longitude = longitude,
                                        cityName = fetchCityName(cityResult.data)
                                    ).let(::updateLocation)
                                }
                                else -> return@launch
                            }
                        }
                    }
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdate() = fusedLocationProviderClient.removeLocationUpdates(locationCallback)

    private fun isDeviceLocationChanged(
        currentLatitude: Double,
        currentLongitude: Double
    ): Boolean {

        val (previousLatitude, previousLongitude) = getPreferredLocation()

        val currentLocation = Location("").apply {
            latitude = currentLatitude
            longitude = currentLongitude
        }

        val previousLocation = Location("").apply {
            latitude = previousLatitude
            longitude = previousLongitude
        }

        val distance = currentLocation.distanceTo(previousLocation)
        return distance > SMALLEST_DISPLACEMENT_METERS
    }

    companion object {
        private val defaultLocation = WeatherLocation(55.751244, 37.618423, "Moscow")
        private const val USE_DEVICE_LOCATION_PREFERENCE = "USE_DEVICE_LOCATION_PREFERENCE"
        private const val PREFERRED_LOCATION = "PREFERRED_LOCATION"
        private const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 15 * 60 * 1000
        private const val SMALLEST_DISPLACEMENT_METERS: Float = 10 * 1000f
    }
}