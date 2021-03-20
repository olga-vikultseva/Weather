package com.example.weather.ui.citysearch

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.ui.citysearch.model.CityItem
import com.example.weather.ui.citysearch.model.CitySearchUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error.view.*
import kotlinx.android.synthetic.main.fragment_city_search.*

@AndroidEntryPoint
class CitySearchFragment : Fragment(R.layout.fragment_city_search) {

    private val viewModel: CitySearchViewModel by viewModels()
    private lateinit var citySearchAdapter: CitySearchAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        bindUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.city_search_menu, menu)
        val searchMenuItem = menu.findItem(R.id.search)
        (searchMenuItem.actionView as androidx.appcompat.widget.SearchView).apply {
            queryHint = getString(R.string.city_search_hint)
            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextChange(query: String): Boolean {
                    viewModel.onQueryTextChanged(query)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.onQueryTextChanged(query)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun bindUI() {

        (requireActivity() as AppCompatActivity).supportActionBar!!.apply {
            title = getString(R.string.city_search_screen_action_bar_title)
            subtitle = getString(R.string.city_search_screen_action_bar_subtitle)
        }

        citySearchAdapter = CitySearchAdapter(cityList = emptyList()) { cityItem ->
            viewModel.onCityItemClicked(cityItem)
            findNavController().navigateUp()
        }

        recyclerView_city_search.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = citySearchAdapter
        }

        viewModel.citySearch.observe(viewLifecycleOwner) {
            when (it) {
                is CitySearchUIState.Empty -> {
                    hideLoading()
                    hideNoMatchesMessage()
                    hideData()
                    hideError()
                }
                is CitySearchUIState.Loading -> {
                    hideNoMatchesMessage()
                    hideData()
                    hideError()
                    showLoading()
                }
                is CitySearchUIState.NoMatches -> {
                    hideLoading()
                    hideData()
                    hideError()
                    showNoMatchesMessage()
                }
                is CitySearchUIState.Data -> {
                    hideLoading()
                    hideNoMatchesMessage()
                    hideError()
                    showData(it.cityList)
                }
                is CitySearchUIState.Error -> {
                    hideLoading()
                    hideNoMatchesMessage()
                    hideData()
                    showError(it.errorMessage)
                }
            }
        }
    }

    private fun showLoading() {
        loading_city_search.isVisible = true
    }

    private fun hideLoading() {
        loading_city_search.isVisible = false
    }

    private fun showNoMatchesMessage() {
        textView_no_matching_cities.isVisible = true
    }

    private fun hideNoMatchesMessage() {
        textView_no_matching_cities.isVisible = false
    }

    private fun showData(cityList: List<CityItem>) {
        citySearchAdapter.apply {
            this.cityList = cityList
            notifyDataSetChanged()
        }
        recyclerView_city_search.isVisible = true
    }

    private fun hideData() {
        recyclerView_city_search.isVisible = false
    }

    private fun showError(errorMessage: String) {
        error_city_search.textView_error_message.text = errorMessage
        error_city_search.isVisible = true
    }

    private fun hideError() {
        error_city_search.isVisible = false
    }
}