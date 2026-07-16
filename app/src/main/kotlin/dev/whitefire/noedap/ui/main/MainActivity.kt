package dev.whitefire.noedap.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.whitefire.noedap.NoedapApplication
import dev.whitefire.noedap.databinding.ActivityMainBinding
import dev.whitefire.noedap.util.formatHours
import dev.whitefire.noedap.util.showTimePicker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (application as NoedapApplication).workDayRepository,
            (application as NoedapApplication).preferencesRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        binding.etStartTime.setOnClickListener {
            it.showTimePicker(this, viewModel.startTime.value ?: LocalTime.of(9, 30)) { time ->
                viewModel.setStartTime(time)
                updateTimeDisplay()
            }
        }

        binding.etEndTime.setOnClickListener {
            it.showTimePicker(this, viewModel.endTime.value ?: LocalTime.of(16, 0)) { time ->
                viewModel.setEndTime(time)
                updateTimeDisplay()
            }
        }

        binding.btnCalculate.setOnClickListener {
            viewModel.saveWorkDay(binding.etNotes.text.toString())
            showToast("Saved")
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteWorkDay()
            showToast("Deleted")
        }

        binding.btnDatePrev.setOnClickListener {
            viewModel.setDate(viewModel.currentDate.value.minusDays(1))
        }

        binding.btnDateNext.setOnClickListener {
            viewModel.setDate(viewModel.currentDate.value.plusDays(1))
        }

        binding.btnToday.setOnClickListener {
            viewModel.setDate(LocalDate.now())
        }

        binding.switchAutoBreak.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoCalculateBreak(isChecked)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentDate.collect { date ->
                        binding.tvDate.text = date.formatShortDate()
                        updateDateButtons(date)
                    }
                }
                launch {
                    viewModel.startTime.collect { time ->
                        binding.etStartTime.setText(time?.formatTime() ?: "")
                        updateTimeDisplay()
                    }
                }
                launch {
                    viewModel.endTime.collect { time ->
                        binding.etEndTime.setText(time?.formatTime() ?: "")
                        updateTimeDisplay()
                    }
                }
                launch {
                    viewModel.breakMinutes.collect { minutes ->
                        binding.tvBreakValue.text = minutes.minutesToDisplayString()
                    }
                }
                launch {
                    viewModel.autoCalculateBreak.collect { enabled ->
                        binding.switchAutoBreak.isChecked = enabled
                    }
                }
                launch {
                    viewModel.stats.collect { stats ->
                        stats?.let { updateStats(it) }
                    }
                }
                launch {
                    viewModel.workTimeConfig.collect { config ->
                        config?.let { updateConfigDisplay(it) }
                    }
                }
            }
        }
    }

    private fun updateTimeDisplay() {
        binding.tvDurationValue.text = viewModel.getCurrentDurationString()
        binding.tvBreakWarning.visibility = if (viewModel.isBreakSufficient()) View.GONE else View.VISIBLE
    }

    private fun updateStats(stats: WorkDayRepository.WeekStats) {
        binding.tvTodayWorkedValue.text = stats.todayHours.formatHours()
        binding.tvWeekWorkedValue.text = "${stats.totalHours.formatHours()} / ${38.5f.formatHours()}"
        binding.tvRemainingValue.text = stats.remainingHours.formatHours()
        binding.progressBar.progress = stats.progressPercentage.toInt()
        binding.tvProgressText.text = "${stats.progressPercentage.toInt()}%"
    }

    private fun updateConfigDisplay(config: WorkTimeConfig) {
        val coreTime = config.coreTimes[viewModel.currentDate.value.dayOfWeek]
        binding.tvKernzeitValue.text = coreTime?.let {
            "${it.start?.formatTime() ?: "--"} - ${it.end?.formatTime() ?: "--"}"
        } ?: "--"
    }

    private fun updateDateButtons(date: LocalDate) {
        binding.btnToday.isEnabled = date != LocalDate.now()
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

class MainViewModelFactory(
    private val workDayRepository: WorkDayRepository,
    private val preferencesRepository: UserPreferencesRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(workDayRepository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
