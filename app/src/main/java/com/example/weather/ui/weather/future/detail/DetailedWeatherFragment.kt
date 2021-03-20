package com.example.weather.ui.weather.future.detail

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.future.detail.model.DetailedWeather
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.additional_temperature_section.view.*
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_detailed_weather.*
import kotlinx.android.synthetic.main.main_temperature_section.view.*
import kotlinx.android.synthetic.main.section_weather_data.view.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class DetailedWeatherFragment : Fragment(R.layout.fragment_detailed_weather) {

    @Inject
    lateinit var assistedFactory: DetailedWeatherViewModel.AssistedFactory
    private val args: DetailedWeatherFragmentArgs by navArgs()
    private val viewModel: DetailedWeatherViewModel by viewModels {
        DetailedWeatherViewModel.provideFactory(
            assistedFactory,
            args.selectedDayOfWeekInt,
            args.cityName
        )
    }
    private lateinit var actionBar: ActionBar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        actionBar = (requireActivity() as AppCompatActivity).supportActionBar!!

        viewModel.detailedWeather.observe(viewLifecycleOwner) {
            when (it) {
                is WeatherUIState.Loading -> {
                    hideData()
                    hideError()
                    showLoading()
                }
                is WeatherUIState.Data -> {
                    hideLoading()
                    hideError()
                    showData(it.data)
                }
                is WeatherUIState.Error -> {
                    hideLoading()
                    hideData()
                    showError(it.errorMessage)
                }
            }
        }
    }

    private fun showLoading() {
        loading_detailed_weather.isVisible = true
    }

    private fun hideLoading() {
        loading_detailed_weather.isVisible = false
    }

    private fun showData(detailedWeather: DetailedWeather) {
        actionBar.apply {
            title = detailedWeather.date
            subtitle = detailedWeather.cityName
        }
        updateDescription(detailedWeather.weatherDescription)
        updateTemperatures(
            detailedWeather.temperatureMorning,
            detailedWeather.temperatureDay,
            detailedWeather.temperatureEvening,
            detailedWeather.temperatureNight,
            detailedWeather.feelsLikeTemperature
        )
        updateWind(detailedWeather.wind)
        updatePressure(detailedWeather.pressure)
        updateHumidity(detailedWeather.humidity)
        updateUvIndex(detailedWeather.uvIndex)
        Glide
            .with(this@DetailedWeatherFragment)
            .load(detailedWeather.weatherIconUrl)
            .into(imageView_weather_icon_detailed_weather)
        group_detailed_weather_data.isVisible = true
    }

    private fun hideData() {
        group_detailed_weather_data.isVisible = false
    }

    private fun showError(errorMessage: String) {
        error_detailed_weather.textView_error_message.text = errorMessage
        error_detailed_weather.isVisible = true
    }

    private fun hideError() {
        error_detailed_weather.isVisible = false
    }

    private fun updateDescription(description: String) {
        textView_weather_description_detailed_weather.text = description.capitalize(Locale.getDefault())
    }

    private fun updateTemperatures(
        tempMorn: String,
        tempDay: String,
        tempEve: String,
        tempNight: String,
        feelsLike: String
    ) {
        main_temperature_section_detailed_weather.textView_temperature.text = tempDay
        main_temperature_section_detailed_weather.textView_feels_like_temperature.text = feelsLike
        additional_temperature_section_detailed_weather.textView_temp_morn_value.text = tempMorn
        additional_temperature_section_detailed_weather.textView_temp_day_value.text = tempDay
        additional_temperature_section_detailed_weather.textView_temp_eve_value.text = tempEve
        additional_temperature_section_detailed_weather.textView_temp_night_value.text = tempNight
    }

    private fun updateWind(wind: String) {
        section_weather_data_detailed_weather.textView_wind.text = wind
    }

    private fun updatePressure(pressure: String) {
        section_weather_data_detailed_weather.textView_pressure.text = pressure
    }

    private fun updateHumidity(humidity: String) {
        section_weather_data_detailed_weather.textView_humidity.text = humidity
    }

    private fun updateUvIndex(uvIndex: String) {
        section_weather_data_detailed_weather.textView_uvi.text = uvIndex
    }
}
