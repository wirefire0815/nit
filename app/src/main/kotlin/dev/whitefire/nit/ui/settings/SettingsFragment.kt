package dev.whitefire.nit.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkTimeConfig
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime

class SettingsFragment : Fragment() {

    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var etWeeklyTarget: EditText
    private lateinit var etBreakAfter: EditText
    private lateinit var etBreakDuration: EditText
    private lateinit var switchEnableCoreHours: MaterialSwitch
    private lateinit var layoutCoreHoursDetails: LinearLayout
    private lateinit var btnMonStart: Button
    private lateinit var btnMonEnd: Button
    private lateinit var btnFriStart: Button
    private lateinit var btnFriEnd: Button

    private val viewModel: SettingsViewModel by viewModels {
        val app = requireActivity().application as NitApplication
        SettingsViewModelFactory(app.preferencesRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSave = view.findViewById(R.id.btnSave)
        btnReset = view.findViewById(R.id.btnReset)
        etWeeklyTarget = view.findViewById(R.id.etWeeklyTarget)
        etBreakAfter = view.findViewById(R.id.etBreakAfter)
        etBreakDuration = view.findViewById(R.id.etBreakDuration)
        switchEnableCoreHours = view.findViewById(R.id.switchEnableCoreHours)
        layoutCoreHoursDetails = view.findViewById(R.id.layoutCoreHoursDetails)
        btnMonStart = view.findViewById(R.id.btnMonStart)
        btnMonEnd = view.findViewById(R.id.btnMonEnd)
        btnFriStart = view.findViewById(R.id.btnFriStart)
        btnFriEnd = view.findViewById(R.id.btnFriEnd)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        btnSave.setOnClickListener { saveSettings() }
        btnReset.setOnClickListener { resetToDefaults() }

        switchEnableCoreHours.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnableCoreHours(isChecked)
            layoutCoreHoursDetails.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        btnMonStart.setOnClickListener {
            showTimePicker(viewModel.config.value?.coreTimes?.get(DayOfWeek.MONDAY)?.start ?: LocalTime.of(9, 30)) { time ->
                viewModel.setCoreTime(DayOfWeek.MONDAY, time, viewModel.config.value?.coreTimes?.get(DayOfWeek.MONDAY)?.end ?: LocalTime.of(16, 0))
            }
        }

        btnMonEnd.setOnClickListener {
            showTimePicker(viewModel.config.value?.coreTimes?.get(DayOfWeek.MONDAY)?.end ?: LocalTime.of(16, 0)) { time ->
                viewModel.setCoreTime(DayOfWeek.MONDAY, viewModel.config.value?.coreTimes?.get(DayOfWeek.MONDAY)?.start ?: LocalTime.of(9, 30), time)
            }
        }

        btnFriStart.setOnClickListener {
            showTimePicker(viewModel.config.value?.coreTimes?.get(DayOfWeek.FRIDAY)?.start ?: LocalTime.of(9, 30)) { time ->
                viewModel.setCoreTime(DayOfWeek.FRIDAY, time, viewModel.config.value?.coreTimes?.get(DayOfWeek.FRIDAY)?.end ?: LocalTime.of(12, 30))
            }
        }

        btnFriEnd.setOnClickListener {
            showTimePicker(viewModel.config.value?.coreTimes?.get(DayOfWeek.FRIDAY)?.end ?: LocalTime.of(12, 30)) { time ->
                viewModel.setCoreTime(DayOfWeek.FRIDAY, viewModel.config.value?.coreTimes?.get(DayOfWeek.FRIDAY)?.start ?: LocalTime.of(9, 30), time)
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.config.collect { config ->
                    config?.let { updateUi(it) }
                }
            }
        }
    }

    private fun updateUi(config: WorkTimeConfig) {
        etWeeklyTarget.setText(config.weeklyTargetHours.toString())
        etBreakAfter.setText(config.breakRules.firstOrNull()?.afterHours?.toString() ?: "6")
        etBreakDuration.setText(config.breakRules.firstOrNull()?.durationHours?.toString() ?: "0.5")

        switchEnableCoreHours.isChecked = config.enableCoreHours
        layoutCoreHoursDetails.visibility = if (config.enableCoreHours) View.VISIBLE else View.GONE

        btnMonStart.text = config.coreTimes[DayOfWeek.MONDAY]?.start?.formatTime() ?: "09:30"
        btnMonEnd.text = config.coreTimes[DayOfWeek.MONDAY]?.end?.formatTime() ?: "16:00"
        btnFriStart.text = config.coreTimes[DayOfWeek.FRIDAY]?.start?.formatTime() ?: "09:30"
        btnFriEnd.text = config.coreTimes[DayOfWeek.FRIDAY]?.end?.formatTime() ?: "12:30"
    }

    private fun saveSettings() {
        val target = etWeeklyTarget.text?.toString()?.toFloatOrNull() ?: 38.5f
        val after = etBreakAfter.text?.toString()?.toFloatOrNull() ?: 6f
        val duration = etBreakDuration.text?.toString()?.toFloatOrNull() ?: 0.5f

        viewModel.setWeeklyTargetHours(target)
        viewModel.setBreakRules(after, duration)
        viewModel.saveConfig()

        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun resetToDefaults() {
        viewModel.resetToDefaults()
        Toast.makeText(requireContext(), "Reset to default settings", Toast.LENGTH_SHORT).show()
    }

    private fun showTimePicker(initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialTime.hour)
            .setMinute(initialTime.minute)
            .setTitleText("Select Core Hour")
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSelected(LocalTime.of(picker.hour, picker.minute))
        }
        picker.show(childFragmentManager, "settings_time_picker")
    }

    private fun LocalTime.formatTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
