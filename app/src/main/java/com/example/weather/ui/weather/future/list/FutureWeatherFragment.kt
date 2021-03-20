package com.example.weather.ui.weather.future.list

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.ui.weather.WeatherUIState
import com.example.weather.ui.weather.future.list.model.FutureWeather
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_future_weather.*

@AndroidEntryPoint
class FutureWeatherFragment : Fragment(R.layout.fragment_future_weather) {

    private val viewModel: FutureWeatherViewModel by viewModels()
    private lateinit var activity: AppCompatActivity
    private lateinit var actionBar: ActionBar
    private lateinit var futureWeatherAdapter: FutureWeatherAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        activity = requireActivity() as AppCompatActivity
        actionBar = activity.supportActionBar!!

        updateDateToNextWeek()

        futureWeatherAdapter = FutureWeatherAdapter(
            context = activity,
            cityName = "",
            futureWeatherList = emptyList()
        ) { futureWeatherItem, cityName ->
            FutureWeatherFragmentDirections.actionFutureWeatherFragmentToDetailedWeatherFragment(
                selectedDayOfWeekInt = futureWeatherItem.dayOfWeekInt,
                cityName = cityName
            ).let(findNavController()::navigate)
        }

        recyclerView_future_weather.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = futureWeatherAdapter
        }

        viewModel.futureWeather.observe(viewLifecycleOwner) {
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

        swipeRefreshLayout_future_weather.setOnRefreshListener {
            viewModel.refreshFutureWeather()
            swipeRefreshLayout_future_weather.isRefreshing = false
        }
    }

    private fun updateDateToNextWeek() {
        actionBar.title = getString(R.string.future_weather_screen_action_bar_title)
    }

    private fun showLoading() {
        loading_future_weather.isVisible = true
    }

    private fun hideLoading() {
        loading_future_weather.isVisible = false
    }

    private fun showData(futureWeather: FutureWeather) {
        actionBar.subtitle = futureWeather.cityName
        futureWeatherAdapter.apply {
            cityName = futureWeather.cityName
            futureWeatherList = futureWeather.data
            notifyDataSetChanged()
        }
        recyclerView_future_weather.isVisible = true
    }

    private fun hideData() {
        recyclerView_future_weather.isVisible = false
    }

    private fun showError(errorMessage: String) {
        error_future_weather.textView_error_message.text = errorMessage
        error_future_weather.isVisible = true
    }

    private fun hideError() {
        error_future_weather.isVisible = false
    }
}
