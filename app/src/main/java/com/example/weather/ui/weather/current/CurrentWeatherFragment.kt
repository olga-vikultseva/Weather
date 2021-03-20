package com.example.weather.ui.weather.current

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.current.model.CurrentWeather
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_current_weather.*
import kotlinx.android.synthetic.main.main_temperature_section.view.*
import kotlinx.android.synthetic.main.section_weather_data.view.*

@AndroidEntryPoint
class CurrentWeatherFragment : Fragment(R.layout.fragment_current_weather) {

    private val viewModel: CurrentWeatherViewModel by viewModels()
    private lateinit var activity: AppCompatActivity
    private lateinit var actionBar: ActionBar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        activity = requireActivity() as AppCompatActivity
        actionBar = activity.supportActionBar!!

        updateDateToToday()

        viewModel.currentWeather.observe(viewLifecycleOwner) {
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

        swipeRefreshLayout_current_weather.setOnRefreshListener {
            viewModel.refreshCurrentWeather()
            swipeRefreshLayout_current_weather.isRefreshing = false
        }
    }

    private fun updateDateToToday() {
        actionBar.title = getString(R.string.current_weather_screen_action_bar_title)
    }

    private fun showLoading() {
        loading_current_weather.isVisible = true
    }

    private fun hideLoading() {
        loading_current_weather.isVisible = false
    }

    private fun showData(currentWeather: CurrentWeather) {
        actionBar.subtitle = currentWeather.cityName
        updateDescription(currentWeather.weatherDescription)
        updateTemperatures(currentWeather.temperature, currentWeather.feelsLikeTemperature)
        updateWind(currentWeather.wind)
        updatePressure(currentWeather.pressure)
        updateHumidity(currentWeather.humidity)
        updateUvIndex(currentWeather.uVIndex)
        Glide
            .with(this@CurrentWeatherFragment)
            .load(currentWeather.weatherIconUrl)
            .into(imageView_weather_icon_current_weather)
        group_current_weather_data.isVisible = true
    }

    private fun hideData() {
        group_current_weather_data.isVisible = false
    }

    private fun showError(errorMessage: String) {
        error_current_weather.textView_error_message.text = errorMessage
        error_current_weather.isVisible = true
    }

    private fun hideError() {
        error_current_weather.isVisible = false
    }

    private fun updateDescription(weatherDescription: String) {
        textView_weather_description_current_weather.text = weatherDescription
    }

    private fun updateTemperatures(temperature: String, feelsLikeTemperature: String) {
        main_temperature_section_current_weather.textView_temperature.text = temperature
        main_temperature_section_current_weather.textView_feels_like_temperature.text =
            feelsLikeTemperature
    }

    private fun updateWind(wind: String) {
        section_weather_data_current_weather.textView_wind.text = wind
    }

    private fun updatePressure(pressure: String) {
        section_weather_data_current_weather.textView_pressure.text = pressure
    }

    private fun updateHumidity(humidity: String) {
        section_weather_data_current_weather.textView_humidity.text = humidity
    }

    private fun updateUvIndex(uvIndex: String) {
        section_weather_data_current_weather.textView_uvi.text = uvIndex
    }
}