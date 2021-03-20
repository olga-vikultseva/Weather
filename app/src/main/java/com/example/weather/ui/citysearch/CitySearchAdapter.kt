package com.example.weather.ui.citysearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.ui.citysearch.model.CityItem
import kotlinx.android.synthetic.main.city_item.view.*

class CitySearchAdapter(
    var cityList: List<CityItem>,
    private val itemClickListener: (CityItem) -> Unit
) : RecyclerView.Adapter<CitySearchAdapter.CityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.city_item, parent, false)
        return CityViewHolder(view)
    }

    override fun getItemCount(): Int = cityList.size

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) =
        holder.bind(cityList[position])

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cityName = itemView.textView_city_name

        init {
            itemView.setOnClickListener {
                itemClickListener(cityList[adapterPosition])
            }
        }

        fun bind(cityItem: CityItem) {
            cityName.text = cityItem.name
        }
    }
}


