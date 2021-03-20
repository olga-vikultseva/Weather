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
import com.example.weather.data.network.CityNetworkDataSource
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
    private val cityNetworkDataSource: CityNetworkDataSource,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : PreferenceProvider(context), LocationProvider {

    private val _useDeviceLocationFlow = MutableStateFlow(isUsingDeviceLocation())
    override val useDeviceLocationFlow = _useDeviceLocationFlow.asStateFlow()

    private val _locationFlow = MutableStateFlow<ResultWrapper<WeatherLocation>>(ResultWrapper.Loading())
    override val locationFlow = _locationFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            USE_DEVICE_LOCATION_PREFERENCE -> {
                _locationFlow.value = ResultWrapper.Loading()
                if (isUsingDeviceLocation()) {
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

    private lateinit var locationCallback: LocationCallback

    init {
        if (isUsingDeviceLocation()) {
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

    override fun isUsingDeviceLocation(): Boolean =
        preferences.getBoolean(USE_DEVICE_LOCATION_PREFERENCE, false)

    override fun updateUseDeviceLocationPreference(value: Boolean) = preferences.edit {
        putBoolean(USE_DEVICE_LOCATION_PREFERENCE, value)
    }

    override fun updateLocationPreference(location: WeatherLocation) = preferences.edit {
        putString(PREFERRED_LOCATION, Json.encodeToString(location))
    }

    override fun persistLocationForLastRequestCurrentWeather(location: WeatherLocation) =
        preferences.edit {
            putString(LOCATION_FOR_LAST_REQUEST_CURRENT_WEATHER, Json.encodeToString(location))
        }

    override fun persistLocationForLastRequestFutureWeather(location: WeatherLocation) =
        preferences.edit {
            putString(LOCATION_FOR_LAST_REQUEST_FUTURE_WEATHER, Json.encodeToString(location))
        }

    override fun getLocationForLastRequestCurrentWeather(): WeatherLocation? =
        preferences.getString(LOCATION_FOR_LAST_REQUEST_CURRENT_WEATHER, null)?.let {
            Json.decodeFromString(it)
        }

    override fun getLocationForLastRequestFutureWeather(): WeatherLocation? =
        preferences.getString(LOCATION_FOR_LAST_REQUEST_FUTURE_WEATHER, null)?.let {
            Json.decodeFromString(it)
        }

    override suspend fun requestLocationUpdate() {
        _locationFlow.value = ResultWrapper.Loading()
        _locationFlow.value = getLocation()
    }

    private fun getPreferredLocation(): WeatherLocation =
        preferences.getString(PREFERRED_LOCATION, null).let {
            if (it == null) defaultLocation else Json.decodeFromString(it)
        }

    private suspend fun getLocation(): ResultWrapper<WeatherLocation> =
        if (isUsingDeviceLocation()) {
            if (hasLocationPermissionGranted()) {
                getDeviceLocationCoordinatesAsync().await()?.let { currentDeviceLocation ->
                    if (isDeviceLocationChanged(
                            currentLatitude = currentDeviceLocation.latitude,
                            currentLongitude = currentDeviceLocation.longitude
                        )
                    ) {
                        cityNetworkDataSource.searchCity(
                            latitude = currentDeviceLocation.latitude,
                            longitude = currentDeviceLocation.longitude
                        ).let { cityQueryResult ->
                            when (cityQueryResult) {
                                is ResultWrapper.Success -> {
                                    val cityName = fetchCityName(cityQueryResult.data)
                                    updateLocationPreference(
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
                updateUseDeviceLocationPreference(false)
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
                        cityNetworkDataSource.searchCity(latitude, longitude).let { cityResult ->
                            when (cityResult) {
                                is ResultWrapper.Success -> {
                                    WeatherLocation(
                                        latitude = latitude,
                                        longitude = longitude,
                                        cityName = fetchCityName(cityResult.data)
                                    ).let(::updateLocationPreference)
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

    private companion object {
        val defaultLocation = WeatherLocation(55.751244, 37.618423, "Moscow")
        const val USE_DEVICE_LOCATION_PREFERENCE = "USE_DEVICE_LOCATION_PREFERENCE"
        const val PREFERRED_LOCATION = "PREFERRED_LOCATION"
        const val LOCATION_FOR_LAST_REQUEST_CURRENT_WEATHER = "LOCATION_FOR_LAST_REQUEST_CURRENT_WEATHER"
        const val LOCATION_FOR_LAST_REQUEST_FUTURE_WEATHER = "LOCATION_FOR_LAST_REQUEST_FUTURE_WEATHER"
        const val LOCATION_UPDATE_INTERVAL_MILLIS: Long = 15 * 60 * 1000
        const val SMALLEST_DISPLACEMENT_METERS: Float = 10 * 1000f
    }
}