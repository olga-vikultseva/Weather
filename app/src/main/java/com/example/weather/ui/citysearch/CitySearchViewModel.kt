package com.example.weather.ui.citysearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.WeatherLocation
import com.example.weather.domain.citysearch.CitySearchInteractor
import com.example.weather.domain.citysearch.model.CitySearchState
import com.example.weather.internal.ErrorFormatter
import com.example.weather.ui.citysearch.model.CityItem
import com.example.weather.ui.citysearch.model.CitySearchUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CitySearchViewModel @Inject constructor(
    private val citySearchInteractor: CitySearchInteractor,
    private val errorFormatter: ErrorFormatter
) : ViewModel() {

    private val _citySearch = MutableLiveData<CitySearchUIState>()
    val citySearch: LiveData<CitySearchUIState>
        get() = _citySearch

    init {
        subscribeToCitySearchUpdates()
    }

    private fun subscribeToCitySearchUpdates() {
        viewModelScope.launch {
            citySearchInteractor.citySearchStateFlow
                .map { citySearchState ->
                    when (citySearchState) {
                        is CitySearchState.Empty -> CitySearchUIState.Empty
                        is CitySearchState.Loading -> CitySearchUIState.Loading
                        is CitySearchState.SearchResult -> convertToCitySearchUIState(
                            citySearchState.cityList
                        )
                        is CitySearchState.Error -> CitySearchUIState.Error(
                            errorFormatter.getErrorMessage(citySearchState.error)
                        )
                    }
                }
                .collect {
                    _citySearch.value = it
                }
        }
    }

    fun onQueryTextChanged(query: String) = citySearchInteractor.searchCity(query)

    fun onCityItemClicked(cityItem: CityItem) = WeatherLocation(
        latitude = cityItem.latitude,
        longitude = cityItem.longitude,
        cityName = cityItem.name
    ).let(citySearchInteractor::updateLocationPreference)

    private fun convertToCitySearchUIState(locationList: List<WeatherLocation>): CitySearchUIState =
        if (locationList.isEmpty()) {
            CitySearchUIState.NoMatches
        } else {
            val cityList = mutableListOf<CityItem>()
            locationList.forEach {
                CityItem(
                    name = it.cityName,
                    latitude = it.latitude,
                    longitude = it.longitude
                ).let(cityList::add)
            }
            CitySearchUIState.Data(cityList)
        }

    override fun onCleared() {
        super.onCleared()
        citySearchInteractor.close()
    }
}
