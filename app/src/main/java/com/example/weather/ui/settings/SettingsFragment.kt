package com.example.weather.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weather.R
import com.example.weather.data.UnitSystem
import com.example.weather.ui.settings.model.LocationSettingUIState
import com.example.weather.ui.settings.model.UnitSystemSettingUIState
import com.example.weather.ui.settings.model.UseDeviceLocationSettingUIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var activity: AppCompatActivity

    private val onCheckedChangeListener: (CompoundButton, Boolean) -> Unit = { _, _ ->
        viewModel.onUseDeviceLocationCheckedChange()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }

    private fun bindUI() {

        activity = (requireActivity() as AppCompatActivity)
        activity.supportActionBar!!.apply {
            title = getString(R.string.settings_screen_action_bar_title)
            subtitle = getString(R.string.settings_screen_action_bar_subtitle)
        }

        with(viewModel) {

            useDeviceLocationSetting.observe(viewLifecycleOwner) {
                updateUseDeviceLocationSetting(it)
            }

            locationSetting.observe(viewLifecycleOwner) {
                updateLocationSetting(it)
            }

            unitSystemSetting.observe(viewLifecycleOwner) {
                updateUnitSystemSetting(it)
            }

            permissionRequestTrigger.observe(viewLifecycleOwner) { (permission, requestCode) ->
                if (permission != null && requestCode != null) {
                    checkPermission(permission, requestCode)
                }
            }
        }

        linearLayout_unit_system_setting.setOnClickListener {
            showDialogForSelectionUnitSystem()
        }
    }

    private fun updateUseDeviceLocationSetting(useDeviceLocationSetting: UseDeviceLocationSettingUIState) {
        when (useDeviceLocationSetting) {
            UseDeviceLocationSettingUIState.Loading -> return
            UseDeviceLocationSettingUIState.Enabled -> switchCompat_use_device_location_setting.apply {
                setOnCheckedChangeListener(null)
                isChecked = true
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
            UseDeviceLocationSettingUIState.Disabled -> switchCompat_use_device_location_setting.apply {
                setOnCheckedChangeListener(null)
                isChecked = false
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
        }
    }

    private fun updateLocationSetting(locationSetting: LocationSettingUIState) {
        when (locationSetting) {
            is LocationSettingUIState.Loading -> {
                textView_location_setting_value.text = getString(R.string.loading)
                linearLayout_location_setting.apply {
                    setOnClickListener(null)
                    isClickable = false
                    alpha = 0.5f
                }
            }
            is LocationSettingUIState.Data -> {
                textView_location_setting_value.text = locationSetting.cityName
                if (locationSetting.isClickable) {
                    linearLayout_location_setting.apply {
                        setOnClickListener {
                            findNavController().navigate(R.id.actionSettingsFragmentToCitySearchFragment)
                        }
                        isClickable = true
                        alpha = 1.0f
                    }
                } else {
                    linearLayout_location_setting.apply {
                        setOnClickListener(null)
                        isClickable = false
                        alpha = 0.5f
                    }
                }
            }
            is LocationSettingUIState.Error -> {
                textView_location_setting_value.text = getString(R.string.error_placeholder, locationSetting.errorMessage)
                linearLayout_location_setting.apply {
                    setOnClickListener {
                        viewModel.requestLocationUpdate()
                    }
                    isClickable = true
                    alpha = 1.0f
                }
            }
        }
    }

    private fun updateUnitSystemSetting(unitSystemSetting: UnitSystemSettingUIState) {
        textView_unit_system_setting_value.text = getString(
            when (unitSystemSetting) {
                UnitSystemSettingUIState.Loading -> R.string.loading
                UnitSystemSettingUIState.Metric -> R.string.unit_system_metric
                UnitSystemSettingUIState.Imperial -> R.string.unit_system_imperial
            }
        )
    }

    private fun showDialogForSelectionUnitSystem() {
        AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.dialog_for_selection_unit_system_title))
            setItems(unitSystems.map { it.first }.toTypedArray()) { _, index ->
                viewModel.onSelectionUnitSystemResult(unitSystems[index].second)
            }
            show()
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        ContextCompat.checkSelfPermission(activity, permission).let { permissionResult ->
            if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                viewModel.onRequestPermissionsResult(
                    requestCode,
                    intArrayOf(PackageManager.PERMISSION_GRANTED)
                )
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    AlertDialog.Builder(activity).apply {
                        setTitle(getString(R.string.permission_request_title))
                        setMessage(getString(R.string.permission_request_message))
                        setPositiveButton(getString(R.string.permission_request_positive_button_text)) { _, _ ->
                            requestPermissions(arrayOf(permission), requestCode)
                        }
                        show()
                    }
                } else requestPermissions(arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) = viewModel.onRequestPermissionsResult(requestCode, grantResults)

    private companion object {
        val unitSystems = arrayOf("Metric" to UnitSystem.METRIC, "Imperial" to UnitSystem.IMPERIAL)
    }
}