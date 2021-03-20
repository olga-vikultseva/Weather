package com.example.weather.ui.weather.future.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.ui.weather.future.list.model.FutureWeatherItem
import kotlinx.android.synthetic.main.future_weather_item.view.*

class FutureWeatherAdapter(
    private val context: Context,
    var cityName: String,
    var futureWeatherList: List<FutureWeatherItem>,
    private val itemClickListener: (FutureWeatherItem, String) -> Unit
) : RecyclerView.Adapter<FutureWeatherAdapter.WeatherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder =
        WeatherViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.future_weather_item, parent, false)
        )

    override fun getItemCount(): Int = futureWeatherList.size

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) =
        holder.bind(futureWeatherList[position])

    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val date = itemView.textView_date_future_weather
        private val dayOfWeek = itemView.textView_day_of_week_name_future_weather
        private val temperature = itemView.textView_temperature_future_weather
        private val weatherDescription = itemView.textView_weather_description_future_weather
        private val weatherIcon = itemView.imageView_weather_icon_future_weather

        init {
            itemView.setOnClickListener {
                itemClickListener(futureWeatherList[adapterPosition], cityName)
            }
        }

        fun bind(futureWeatherItem: FutureWeatherItem) {

            date.text = futureWeatherItem.date
            dayOfWeek.text = futureWeatherItem.dayOfWeekName
            temperature.text = futureWeatherItem.temperature
            weatherDescription.text = futureWeatherItem.weatherDescription

            Glide
                .with(context)
                .load(futureWeatherItem.weatherIconUrl)
                .into(weatherIcon)
        }
    }
}

