package com.example.weather.ui.weather.future.detail

import androidx.lifecycle.*
import com.example.weather.R
import com.example.weather.data.db.entity.future.FutureWeatherEntry
import com.example.weather.domain.weather.future.detail.DetailedWeatherInteractor
import com.example.weather.internal.MetricFormatter
import com.example.weather.internal.StringProvider
import com.example.weather.internal.formatMillis
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.future.detail.model.DetailedWeather
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailedWeatherViewModel @AssistedInject constructor(
    @Assisted private val dayOfWeek: Int,
    @Assisted private val cityName: String,
    private val detailedWeatherInteractor: DetailedWeatherInteractor,
    private val stringProvider: StringProvider,
    private val metricFormatter: MetricFormatter,
    private val simpleDateFormat: SimpleDateFormat
) : ViewModel() {

    private val _detailedWeather = MutableLiveData<WeatherUIState<DetailedWeather>>()
    val detailedWeather: LiveData<WeatherUIState<DetailedWeather>>
        get() = _detailedWeather

    init {
        _detailedWeather.value = WeatherUIState.Loading()
        viewModelScope.launch {
            detailedWeatherInteractor.getDetailedWeather(dayOfWeek).let {
                _detailedWeather.value = WeatherUIState.Data(convertToDetailedWeather(it))
            }
        }
    }

    private fun convertToDetailedWeather(futureWeatherEntry: FutureWeatherEntry): DetailedWeather =
        DetailedWeather(
            cityName = cityName,
            date = simpleDateFormat.formatMillis(futureWeatherEntry.timestampMillis),
            weatherDescription = futureWeatherEntry.day.weatherDescription.capitalize(Locale.getDefault()),
            temperatureMorning = metricFormatter.getFormattedTemperature(futureWeatherEntry.day.tempMorn),
            temperatureDay = metricFormatter.getFormattedTemperature(futureWeatherEntry.day.tempDay),
            temperatureEvening = metricFormatter.getFormattedTemperature(futureWeatherEntry.day.tempEve),
            temperatureNight = metricFormatter.getFormattedTemperature(futureWeatherEntry.day.tempNight),
            feelsLikeTemperature = stringProvider.getString(
                R.string.feels_like_temp_placeholder,
                metricFormatter.getFormattedTemperature(futureWeatherEntry.day.feelsLikeDay)
            ),
            wind = stringProvider.getString(
                R.string.wind_placeholder,
                futureWeatherEntry.day.windDirection,
                metricFormatter.getFormattedWindSpeed(futureWeatherEntry.day.windSpeed)
            ),
            pressure = stringProvider.getString(
                R.string.pressure_placeholder,
                futureWeatherEntry.day.pressure
            ),
            humidity = stringProvider.getString(
                R.string.humidity_placeholder,
                futureWeatherEntry.day.humidity
            ),
            uvIndex = stringProvider.getString(
                R.string.uv_index_placeholder,
                futureWeatherEntry.day.uVIndex.toInt()
            ),
            weatherIconUrl = futureWeatherEntry.day.weatherIconUrl
        )

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(dayOfWeek: Int, cityName: String): DetailedWeatherViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            dayOfWeek: Int,
            cityName: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                assistedFactory.create(dayOfWeek, cityName) as T
        }
    }
}
