package com.example.weather.ui.weather.current

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.entity.current.CurrentWeatherEntry
import com.example.weather.domain.weather.WeatherState
import com.example.weather.domain.weather.current.CurrentWeatherInteractor
import com.example.weather.internal.ErrorFormatter
import com.example.weather.internal.MetricFormatter
import com.example.weather.internal.StringProvider
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.current.model.CurrentWeather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CurrentWeatherViewModel @Inject constructor(
    private val currentWeatherInteractor: CurrentWeatherInteractor,
    private val stringProvider: StringProvider,
    private val metricFormatter: MetricFormatter,
    private val errorFormatter: ErrorFormatter
) : ViewModel() {

    val currentWeather = liveData {
        currentWeatherInteractor.currentWeatherStateFlow
            .map { currentWeatherState ->
                when (currentWeatherState) {
                    is WeatherState.Loading -> WeatherUIState.Loading()
                    is WeatherState.Data -> WeatherUIState.Data(
                        convertToCurrentWeather(
                            currentWeatherEntry = currentWeatherState.data,
                            weatherLocation = currentWeatherState.weatherLocation
                        )
                    )
                    is WeatherState.Error -> WeatherUIState.Error(
                        errorFormatter.getErrorMessage(currentWeatherState.error)
                    )
                }
            }
            .collect { currentWeatherUIState ->
                emit(currentWeatherUIState)
            }
    }

    init {
        viewModelScope.launch {
            currentWeatherInteractor.subscribeToCurrentWeatherUpdates()
        }
    }

    fun refreshCurrentWeather() {
        viewModelScope.launch {
            currentWeatherInteractor.refreshCurrentWeather()
        }
    }

    private fun convertToCurrentWeather(
        currentWeatherEntry: CurrentWeatherEntry,
        weatherLocation: WeatherLocation
    ): CurrentWeather = CurrentWeather(
        cityName = weatherLocation.cityName,
        weatherDescription = currentWeatherEntry.weatherDescription.capitalize(Locale.getDefault()),
        temperature = metricFormatter.getFormattedTemperature(currentWeatherEntry.temperature),
        feelsLikeTemperature = stringProvider.getString(
            R.string.feels_like_temp_placeholder,
            metricFormatter.getFormattedTemperature(currentWeatherEntry.feelsLikeTemperature)
        ),
        wind = stringProvider.getString(
            R.string.wind_placeholder,
            currentWeatherEntry.windDirection,
            metricFormatter.getFormattedWindSpeed(currentWeatherEntry.windSpeed)
        ),
        pressure = stringProvider.getString(
            R.string.pressure_placeholder,
            currentWeatherEntry.pressure
        ),
        humidity = stringProvider.getString(
            R.string.humidity_placeholder,
            currentWeatherEntry.humidity
        ),
        uvIndex = stringProvider.getString(
            R.string.uv_index_placeholder,
            currentWeatherEntry.uvIndex
        ),
        weatherIconUrl = currentWeatherEntry.weatherIconUrl
    )
}
