package com.example.weather.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.weather.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = Navigation.findNavController(this, R.id.nav_host_fragment).apply {
            addOnDestinationChangedListener { _, destination, _ ->
                BottomNavigationView.isVisible = when (destination.id) {
                    R.id.current_weather_fragment,
                    R.id.future_weather_fragment,
                    R.id.settings_fragment -> true
                    else -> false
                }
            }
        }

        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(
                topLevelDestinationIds = setOf(
                    R.id.current_weather_fragment,
                    R.id.future_weather_fragment,
                    R.id.settings_fragment
                )
            )
        )

        BottomNavigationView.setupWithNavController(navController)
    }
}
