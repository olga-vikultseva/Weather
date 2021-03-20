package com.example.weather.ui.weather.future.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.WeatherLocation
import com.example.weather.data.db.entity.future.FutureWeatherEntry
import com.example.weather.domain.weather.WeatherState
import com.example.weather.domain.weather.future.list.FutureWeatherInteractor
import com.example.weather.internal.ErrorFormatter
import com.example.weather.internal.MetricFormatter
import com.example.weather.internal.formatMillis
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.future.list.model.FutureWeather
import com.example.weather.ui.weather.future.list.model.FutureWeatherItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FutureWeatherViewModel @Inject constructor(
    private val futureWeatherInteractor: FutureWeatherInteractor,
    private val metricFormatter: MetricFormatter,
    private val errorFormatter: ErrorFormatter,
    private val simpleDateFormat: SimpleDateFormat
) : ViewModel() {

    private val _futureWeather = MutableLiveData<WeatherUIState<FutureWeather>>()
    val futureWeather: LiveData<WeatherUIState<FutureWeather>>
        get() = _futureWeather

    init {
        subscribeToFutureWeatherUpdates()
    }

    fun refreshFutureWeather() = futureWeatherInteractor.refreshFutureWeather()

    private fun subscribeToFutureWeatherUpdates() {
        viewModelScope.launch {
            futureWeatherInteractor.futureWeatherStateFlow
                .map { futureWeatherState ->
                    when (futureWeatherState) {
                        is WeatherState.Loading -> WeatherUIState.Loading()
                        is WeatherState.Data -> WeatherUIState.Data(
                            convertToFutureWeather(
                                futureWeatherEntries = futureWeatherState.data,
                                weatherLocation = futureWeatherState.weatherLocation
                            )
                        )
                        is WeatherState.Error -> WeatherUIState.Error(
                            errorFormatter.getErrorMessage(futureWeatherState.error)
                        )
                    }
                }
                .collect {
                    _futureWeather.value = it
                }
        }
    }

    private fun convertToFutureWeather(
        futureWeatherEntries: List<FutureWeatherEntry>,
        weatherLocation: WeatherLocation
    ): FutureWeather {

        val futureWeatherList = mutableListOf<FutureWeatherItem>()
        val sortedFutureWeatherEntries = sortFutureWeatherEntries(futureWeatherEntries)

        sortedFutureWeatherEntries.forEach {
            FutureWeatherItem(
                date = simpleDateFormat.formatMillis(it.timestampMillis),
                dayOfWeekInt = it.dayOfWeekInt,
                dayOfWeekName = DateFormatSymbols.getInstance().weekdays[it.dayOfWeekInt],
                temperature = metricFormatter.getFormattedTemperature(it.day.tempDay),
                weatherDescription = it.day.weatherDescription.capitalize(Locale.getDefault()),
                weatherIconUrl = it.day.weatherIconUrl
            ).let(futureWeatherList::add)
        }

        return FutureWeather(cityName = weatherLocation.cityName, data = futureWeatherList)
    }

    private fun sortFutureWeatherEntries(futureWeatherEntries: List<FutureWeatherEntry>): List<FutureWeatherEntry> {

        val sortedFutureWeatherEntries = mutableListOf<FutureWeatherEntry>()
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val sortedWeek = sortWeekStartingFromCurrentDay(today)

        sortedWeek.forEach { dayOfWeek ->
            futureWeatherEntries.find {
                it.dayOfWeekInt == dayOfWeek
            }?.let(sortedFutureWeatherEntries::add)
        }

        return sortedFutureWeatherEntries
    }

    @Suppress("UNCHECKED_CAST")
    private fun sortWeekStartingFromCurrentDay(currentDay: Int): Array<Int> {

        val sortedWeek = arrayOfNulls<Int>(7)
        val firstDayIndex = daysOfWeek.indexOf(currentDay)
        var index = 0

        for (i in firstDayIndex..daysOfWeek.lastIndex) {
            sortedWeek[index] = daysOfWeek[i]
            index++
        }

        for (i in 0 until firstDayIndex) {
            sortedWeek[index] = daysOfWeek[i]
            index++
        }

        return sortedWeek as Array<Int>
    }

    override fun onCleared() {
        super.onCleared()
        futureWeatherInteractor.close()
    }

    private companion object {
        val daysOfWeek = arrayOf(1, 2, 3, 4, 5, 6, 7)
    }
}
