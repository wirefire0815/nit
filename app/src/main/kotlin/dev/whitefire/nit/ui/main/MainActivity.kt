package dev.whitefire.nit.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkDay
import dev.whitefire.nit.domain.model.WorkTimeConfig
import dev.whitefire.nit.ui.history.HistoryActivity
import dev.whitefire.nit.ui.settings.SettingsActivity
import dev.whitefire.nit.util.formatDate
import dev.whitefire.nit.util.formatHours
import dev.whitefire.nit.util.formatTime
import dev.whitefire.nit.util.showTimePicker
import dev.whitefire.nit.util.showToast
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            (application as NitApplication).workDayRepository,
            (application as NitApplication).preferencesRepository
        )
    }

    private lateinit var btnStartTime: MaterialButton
    private lateinit var btnEndTime: MaterialButton
    private lateinit var btnBreakMins: MaterialButton
    private lateinit var etNotes: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnDeleteEntry: Button
    private lateinit var btnDatePrev: Button
    private lateinit var btnDateNext: Button
    private lateinit var btnToday: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvDate: TextView
    private lateinit var tvDurationValue: TextView
    private lateinit var tvBreakValue: TextView
    private lateinit var tvTodayWorkedValue: TextView
    private lateinit var tvWeekWorkedValue: TextView
    private lateinit var tvRemainingValue: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var tvProgressText: TextView
    private lateinit var tvPlannerSummary: TextView
    private lateinit var tvLeaveSuggestion: TextView
    private lateinit var tvTargetHoursBadge: TextView
    private lateinit var tvCoreHoursBadge: TextView
    private lateinit var toggleFridayMode: MaterialButtonToggleGroup
    private lateinit var btnModeEarlyFriday: Button
    private lateinit var btnModeBalanced: Button
    private lateinit var tvMonHours: TextView
    private lateinit var tvTueHours: TextView
    private lateinit var tvWedHours: TextView
    private lateinit var tvThuHours: TextView
    private lateinit var tvFriHours: TextView

    private var onboardingDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        btnStartTime = findViewById(R.id.btnStartTime)
        btnEndTime = findViewById(R.id.btnEndTime)
        btnBreakMins = findViewById(R.id.btnBreakMins)
        etNotes = findViewById(R.id.etNotes)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnDeleteEntry = findViewById(R.id.btnDeleteEntry)
        btnDatePrev = findViewById(R.id.btnDatePrev)
        btnDateNext = findViewById(R.id.btnDateNext)
        btnToday = findViewById(R.id.btnToday)
        bottomNav = findViewById(R.id.bottomNav)
        tvDate = findViewById(R.id.tvDate)
        tvDurationValue = findViewById(R.id.tvDurationValue)
        tvBreakValue = findViewById(R.id.tvBreakValue)
        tvTodayWorkedValue = findViewById(R.id.tvTodayWorkedValue)
        tvWeekWorkedValue = findViewById(R.id.tvWeekWorkedValue)
        tvRemainingValue = findViewById(R.id.tvRemainingValue)
        progressBar = findViewById(R.id.progressBar)
        tvProgressText = findViewById(R.id.tvProgressText)
        tvPlannerSummary = findViewById(R.id.tvPlannerSummary)
        tvLeaveSuggestion = findViewById(R.id.tvLeaveSuggestion)
        tvTargetHoursBadge = findViewById(R.id.tvTargetHoursBadge)
        tvCoreHoursBadge = findViewById(R.id.tvCoreHoursBadge)
        toggleFridayMode = findViewById(R.id.toggleFridayMode)
        btnModeEarlyFriday = findViewById(R.id.btnModeEarlyFriday)
        btnModeBalanced = findViewById(R.id.btnModeBalanced)
        tvMonHours = findViewById(R.id.tvMonHours)
        tvTueHours = findViewById(R.id.tvTueHours)
        tvWedHours = findViewById(R.id.tvWedHours)
        tvThuHours = findViewById(R.id.tvThuHours)
        tvFriHours = findViewById(R.id.tvFriHours)
    }

    private fun setupListeners() {
        btnStartTime.setOnClickListener {
            btnStartTime.showTimePicker(this, viewModel.startTime.value ?: LocalTime.of(9, 30)) { time ->
                viewModel.setStartTime(time)
                updateUI()
            }
        }

        btnEndTime.setOnClickListener {
            btnEndTime.showTimePicker(this, viewModel.endTime.value ?: LocalTime.of(16, 0)) { time ->
                viewModel.setEndTime(time)
                updateUI()
            }
        }

        btnBreakMins.setOnClickListener {
            val options = arrayOf("0m", "15m", "30m", "45m", "60m")
            val values = intArrayOf(0, 15, 30, 45, 60)
            AlertDialog.Builder(this)
                .setTitle("Select Break Duration")
                .setItems(options) { _, which ->
                    viewModel.setBreakMinutes(values[which])
                    updateUI()
                }
                .show()
        }

        etNotes.addTextChangedListener { text ->
            viewModel.setNotes(text?.toString() ?: "")
        }

        btnCalculate.setOnClickListener {
            viewModel.saveWorkDay()
            showToast("Work day entry saved")
        }

        btnDeleteEntry.setOnClickListener {
            viewModel.deleteWorkDay()
            showToast("Work day entry deleted")
        }

        btnDatePrev.setOnClickListener {
            viewModel.setDate(viewModel.currentDate.value.minusDays(1))
        }

        btnDateNext.setOnClickListener {
            viewModel.setDate(viewModel.currentDate.value.plusDays(1))
        }

        btnToday.setOnClickListener {
            viewModel.setDate(LocalDate.now())
        }

        toggleFridayMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnModeBalanced -> viewModel.setFridayExitMode(WorkTimeConfig.SchedulePlannerMode.BALANCED)
                    R.id.btnModeEarlyFriday -> viewModel.setFridayExitMode(WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS)
                }
                updateUI()
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    false
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isOnboardingCompleted.collect { completed ->
                        if (!completed && !onboardingDialogShown) {
                            showOnboardingWizard()
                        }
                    }
                }

                launch {
                    viewModel.workTimeConfig.collect { config ->
                        config?.let {
                            if (it.plannerMode == WorkTimeConfig.SchedulePlannerMode.BALANCED) {
                                toggleFridayMode.check(R.id.btnModeBalanced)
                            } else {
                                toggleFridayMode.check(R.id.btnModeEarlyFriday)
                            }
                            updateUI()
                        }
                    }
                }

                launch {
                    viewModel.currentDate.collect { date ->
                        tvDate.text = date.formatDate()
                        updateUI()
                    }
                }

                launch {
                    viewModel.currentWeek.collect { updateUI() }
                }
            }
        }
    }

    private fun updateUI() {
        val start = viewModel.startTime.value
        val end = viewModel.endTime.value
        val breakMins = viewModel.breakMinutes.value
        val config = viewModel.workTimeConfig.value ?: return

        btnStartTime.text = start?.formatTime() ?: "--:--"
        btnEndTime.text = end?.formatTime() ?: "--:--"
        btnBreakMins.text = "${breakMins}m"

        // Gross, Net, and Break metrics
        if (start != null && end != null) {
            val grossDuration = java.time.Duration.between(start, end)
            if (!grossDuration.isNegative) {
                val grossMins = grossDuration.toMinutes().toInt()
                val netMins = (grossMins - breakMins).coerceAtLeast(0)

                tvTodayWorkedValue.text = String.format("%02dh %02dm", grossMins / 60, grossMins % 60)
                tvDurationValue.text = String.format("%02dh %02dm", netMins / 60, netMins % 60)
                tvBreakValue.text = "${breakMins}m"

                // Check Core Hours compliance if enabled
                if (config.enableCoreHours) {
                    val tempDay = WorkDay(date = viewModel.currentDate.value, startTime = start, endTime = end, breakMinutes = breakMins)
                    val core = config.coreTimes[tempDay.dayOfWeek]
                    if (core?.start != null && core.end != null) {
                        tvCoreHoursBadge.visibility = View.VISIBLE
                        if (tempDay.satisfiesCoreHours(config)) {
                            tvCoreHoursBadge.text = "Core Hours Met ✓ (${core.start.formatTime()} - ${core.end.formatTime()})"
                            tvCoreHoursBadge.setBackgroundColor(getColor(R.color.success_bg))
                            tvCoreHoursBadge.setTextColor(getColor(R.color.success_green))
                        } else {
                            tvCoreHoursBadge.text = "Core Hours Warning: Mandatory presence (${core.start.formatTime()} - ${core.end.formatTime()})"
                            tvCoreHoursBadge.setBackgroundColor(getColor(R.color.warning_bg))
                            tvCoreHoursBadge.setTextColor(getColor(R.color.warning_amber))
                        }
                    } else {
                        tvCoreHoursBadge.visibility = View.GONE
                    }
                } else {
                    tvCoreHoursBadge.visibility = View.GONE
                }
            } else {
                resetDayInputs()
            }
        } else {
            resetDayInputs()
        }

        // Update Smart Schedule Planner & Projections
        val projection = viewModel.getScheduleProjection()
        projection?.let { proj ->
            tvPlannerSummary.text = proj.summaryText

            val targetMins = Math.round(proj.todayTargetHours * 60)
            tvTargetHoursBadge.text = String.format("Target: %dh %02dm", targetMins / 60, targetMins % 60)

            val suggestedLeave = viewModel.getSuggestedLeaveTime()
            tvLeaveSuggestion.text = if (suggestedLeave != null) {
                "Suggested Leave: ${suggestedLeave.formatTime()}"
            } else {
                "Suggested Leave: --:--"
            }
        }

        // Update Week Progress & Days breakdown
        val stats = viewModel.getCurrentWeekStatsPreview()
        val totalMins = Math.round(stats.totalHours * 60)
        val targetMins = Math.round(config.weeklyTargetHours * 60)
        tvWeekWorkedValue.text = String.format("%02dh %02dm / %02dh %02dm", totalMins / 60, totalMins % 60, targetMins / 60, targetMins % 60)

        val remMins = Math.round(stats.remainingHours.coerceAtLeast(0f) * 60)
        tvRemainingValue.text = String.format("Remaining: %02dh %02dm", remMins / 60, remMins % 60)

        progressBar.progress = stats.progressPercentage.toInt()
        tvProgressText.text = "${stats.progressPercentage.toInt()}% of weekly goal"

        // Day breakdown
        val weekDist = viewModel.getWeekDistribution()
        val dayViews = listOf(tvMonHours, tvTueHours, tvWedHours, tvThuHours, tvFriHours)
        for (i in dayViews.indices) {
            val dist = weekDist.getOrNull(i)
            if (dist != null && dist.hours > 0f) {
                val dm = Math.round(dist.hours * 60)
                dayViews[i].text = String.format("%02dh %02dm", dm / 60, dm % 60)
            } else {
                dayViews[i].text = "--:--"
            }
        }
    }

    private fun resetDayInputs() {
        tvTodayWorkedValue.text = "00h 00m"
        tvDurationValue.text = "00h 00m"
        tvBreakValue.text = "0m"
        tvCoreHoursBadge.visibility = View.GONE
    }

    private fun showOnboardingWizard() {
        onboardingDialogShown = true
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_onboarding, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etOnboardingTargetHours)
        val switchCoreHours = dialogView.findViewById<MaterialSwitch>(R.id.switchOnboardingCoreHours)
        val btnGetStarted = dialogView.findViewById<Button>(R.id.btnOnboardingGetStarted)
        val toggleFriday = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupOnboardingFriday)

        toggleFriday.check(R.id.btnOnboardingEarlyFriday)

        btnGetStarted.setOnClickListener {
            val target = etTarget.text?.toString()?.toFloatOrNull() ?: 38.5f
            val coreHoursEnabled = switchCoreHours.isChecked
            val plannerMode = if (toggleFriday.checkedButtonId == R.id.btnOnboardingEarlyFriday) {
                WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS
            } else {
                WorkTimeConfig.SchedulePlannerMode.BALANCED
            }

            val currentConfig = viewModel.workTimeConfig.value ?: WorkTimeConfig()
            val newConfig = currentConfig.copy(
                weeklyTargetHours = target,
                enableCoreHours = coreHoursEnabled,
                plannerMode = plannerMode
            )
            (application as NitApplication).preferencesRepository.run {
                kotlinx.coroutines.GlobalScope.launch {
                    setWorkTimeConfig(newConfig)
                    setOnboardingCompleted(true)
                }
            }
            viewModel.completeOnboarding()
            dialog.dismiss()
            updateUI()
        }

        dialog.show()
    }
}
