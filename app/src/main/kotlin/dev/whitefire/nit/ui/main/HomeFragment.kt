package dev.whitefire.nit.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkDay
import dev.whitefire.nit.domain.model.WorkTimeConfig
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var btnStartTime: MaterialButton
    private lateinit var btnEndTime: MaterialButton
    private lateinit var btnBreakMins: MaterialButton
    private lateinit var etNotes: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnDeleteEntry: Button

    private lateinit var btnDatePrev: MaterialButton
    private lateinit var btnDateNext: MaterialButton
    private lateinit var btnToday: MaterialButton

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

    private lateinit var cardMon: MaterialCardView
    private lateinit var cardTue: MaterialCardView
    private lateinit var cardWed: MaterialCardView
    private lateinit var cardThu: MaterialCardView
    private lateinit var cardFri: MaterialCardView

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as NitApplication
        MainViewModelFactory(app.workDayRepository, app.preferencesRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        btnStartTime = view.findViewById(R.id.btnStartTime)
        btnEndTime = view.findViewById(R.id.btnEndTime)
        btnBreakMins = view.findViewById(R.id.btnBreakMins)
        etNotes = view.findViewById(R.id.etNotes)
        btnCalculate = view.findViewById(R.id.btnCalculate)
        btnDeleteEntry = view.findViewById(R.id.btnDeleteEntry)
        btnDatePrev = view.findViewById(R.id.btnDatePrev)
        btnDateNext = view.findViewById(R.id.btnDateNext)
        btnToday = view.findViewById(R.id.btnToday)
        tvDate = view.findViewById(R.id.tvDate)
        tvDurationValue = view.findViewById(R.id.tvDurationValue)
        tvBreakValue = view.findViewById(R.id.tvBreakValue)
        tvTodayWorkedValue = view.findViewById(R.id.tvTodayWorkedValue)
        tvWeekWorkedValue = view.findViewById(R.id.tvWeekWorkedValue)
        tvRemainingValue = view.findViewById(R.id.tvRemainingValue)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        tvPlannerSummary = view.findViewById(R.id.tvPlannerSummary)
        tvLeaveSuggestion = view.findViewById(R.id.tvLeaveSuggestion)
        tvTargetHoursBadge = view.findViewById(R.id.tvTargetHoursBadge)
        tvCoreHoursBadge = view.findViewById(R.id.tvCoreHoursBadge)
        toggleFridayMode = view.findViewById(R.id.toggleFridayMode)
        btnModeEarlyFriday = view.findViewById(R.id.btnModeEarlyFriday)
        btnModeBalanced = view.findViewById(R.id.btnModeBalanced)
        tvMonHours = view.findViewById(R.id.tvMonHours)
        tvTueHours = view.findViewById(R.id.tvTueHours)
        tvWedHours = view.findViewById(R.id.tvWedHours)
        tvThuHours = view.findViewById(R.id.tvThuHours)
        tvFriHours = view.findViewById(R.id.tvFriHours)
        cardMon = view.findViewById(R.id.cardMon)
        cardTue = view.findViewById(R.id.cardTue)
        cardWed = view.findViewById(R.id.cardWed)
        cardThu = view.findViewById(R.id.cardThu)
        cardFri = view.findViewById(R.id.cardFri)
    }

    private fun setupListeners() {
        val dayCards = listOf(cardMon, cardTue, cardWed, cardThu, cardFri)
        dayCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                val weekStart = viewModel.currentWeek.value?.startDate ?: LocalDate.now()
                val targetDate = weekStart.plusDays(index.toLong())
                viewModel.setDate(targetDate)
            }
        }

        btnStartTime.setOnClickListener {
            showTimePicker(viewModel.startTime.value ?: LocalTime.of(9, 30)) { time ->
                viewModel.setStartTime(time)
                updateUI()
            }
        }

        btnEndTime.setOnClickListener {
            showTimePicker(viewModel.endTime.value ?: LocalTime.of(16, 0)) { time ->
                viewModel.setEndTime(time)
                updateUI()
            }
        }

        btnBreakMins.setOnClickListener {
            val options = arrayOf("0m", "15m", "30m", "45m", "60m")
            val values = intArrayOf(0, 15, 30, 45, 60)
            AlertDialog.Builder(requireContext())
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
            Toast.makeText(requireContext(), "Work day entry saved", Toast.LENGTH_SHORT).show()
        }

        btnDeleteEntry.setOnClickListener {
            viewModel.deleteWorkDay()
            Toast.makeText(requireContext(), "Work day entry deleted", Toast.LENGTH_SHORT).show()
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
                val selectedMode = when (checkedId) {
                    R.id.btnModeBalanced -> WorkTimeConfig.SchedulePlannerMode.BALANCED
                    R.id.btnModeEarlyFriday -> WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS
                    else -> null
                }
                if (selectedMode != null && viewModel.workTimeConfig.value?.plannerMode != selectedMode) {
                    viewModel.setFridayExitMode(selectedMode)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.currentDate.collect { updateUI() } }
                launch { viewModel.startTime.collect { updateUI() } }
                launch { viewModel.endTime.collect { updateUI() } }
                launch { viewModel.breakMinutes.collect { updateUI() } }
                launch { viewModel.notes.collect { if (etNotes.text.toString() != it) etNotes.setText(it) } }
                launch { viewModel.workTimeConfig.collect { updateUI() } }
                launch { viewModel.currentWeek.collect { updateUI() } }
            }
        }
    }

    private fun updateUI() {
        val currentDate = viewModel.currentDate.value
        val config = viewModel.workTimeConfig.value ?: return

        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH)
        tvDate.text = currentDate.format(formatter)

        val start = viewModel.startTime.value
        val end = viewModel.endTime.value
        val breakMins = viewModel.breakMinutes.value

        btnStartTime.text = start?.formatTime() ?: "--:--"
        btnEndTime.text = end?.formatTime() ?: "--:--"
        btnBreakMins.text = "${breakMins}m"

        val targetCheckId = when (config.plannerMode) {
            WorkTimeConfig.SchedulePlannerMode.BALANCED -> R.id.btnModeBalanced
            WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS -> R.id.btnModeEarlyFriday
            WorkTimeConfig.SchedulePlannerMode.CUSTOM -> View.NO_ID
        }
        if (targetCheckId != View.NO_ID && toggleFridayMode.checkedButtonId != targetCheckId) {
            toggleFridayMode.check(targetCheckId)
        }

        if (start != null && end != null) {
            val grossDuration = java.time.Duration.between(start, end)
            if (!grossDuration.isNegative) {
                val grossMins = grossDuration.toMinutes().toInt()
                val netMins = (grossMins - breakMins).coerceAtLeast(0)

                tvTodayWorkedValue.text = String.format("%02dh %02dm", grossMins / 60, grossMins % 60)
                tvDurationValue.text = String.format("%02dh %02dm", netMins / 60, netMins % 60)
                tvBreakValue.text = "${breakMins}m"

                if (config.enableCoreHours) {
                    val tempDay = WorkDay(date = currentDate, startTime = start, endTime = end, breakMinutes = breakMins)
                    val core = config.coreTimes[tempDay.dayOfWeek]
                    if (core?.start != null && core.end != null) {
                        tvCoreHoursBadge.visibility = View.VISIBLE
                        if (tempDay.satisfiesCoreHours(config)) {
                            tvCoreHoursBadge.text = "Core Hours Met ✓ (${core.start.formatTime()} - ${core.end.formatTime()})"
                            tvCoreHoursBadge.setBackgroundColor(requireContext().getColor(R.color.success_bg))
                            tvCoreHoursBadge.setTextColor(requireContext().getColor(R.color.success_green))
                        } else {
                            tvCoreHoursBadge.text = "Core Hours Warning: Mandatory presence (${core.start.formatTime()} - ${core.end.formatTime()})"
                            tvCoreHoursBadge.setBackgroundColor(requireContext().getColor(R.color.warning_bg))
                            tvCoreHoursBadge.setTextColor(requireContext().getColor(R.color.warning_amber))
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

        val stats = viewModel.getCurrentWeekStatsPreview()
        val totalMins = Math.round(stats.totalHours * 60)
        val targetMins = Math.round(config.weeklyTargetHours * 60)
        tvWeekWorkedValue.text = String.format("%02dh %02dm / %02dh %02dm", totalMins / 60, totalMins % 60, targetMins / 60, targetMins % 60)

        val remMins = Math.round(stats.remainingHours.coerceAtLeast(0f) * 60)
        tvRemainingValue.text = String.format("Remaining: %02dh %02dm", remMins / 60, remMins % 60)

        progressBar.progress = stats.progressPercentage.toInt()
        tvProgressText.text = "${stats.progressPercentage.toInt()}% of weekly goal"

        val weekDist = viewModel.getWeekDistribution()
        val dayViews = listOf(tvMonHours, tvTueHours, tvWedHours, tvThuHours, tvFriHours)
        val dayCards = listOf(cardMon, cardTue, cardWed, cardThu, cardFri)
        val currentDay = viewModel.currentDate.value
        val weekStart = viewModel.currentWeek.value?.startDate ?: LocalDate.now()
        val activeColor = requireContext().getColor(R.color.primary_slate)
        val defaultBorderColor = requireContext().getColor(R.color.surface_border)
        val borderWidthActive = (2 * resources.displayMetrics.density).toInt()
        val borderWidthDefault = (1 * resources.displayMetrics.density).toInt()

        for (i in dayViews.indices) {
            val dist = weekDist.getOrNull(i)
            if (dist != null && dist.hours > 0f) {
                val dm = Math.round(dist.hours * 60)
                dayViews[i].text = String.format("%02dh %02dm", dm / 60, dm % 60)
            } else {
                dayViews[i].text = "--:--"
            }

            val cardDate = weekStart.plusDays(i.toLong())
            if (cardDate == currentDay) {
                dayCards[i].strokeColor = activeColor
                dayCards[i].strokeWidth = borderWidthActive
            } else {
                dayCards[i].strokeColor = defaultBorderColor
                dayCards[i].strokeWidth = borderWidthDefault
            }
        }
    }

    private fun resetDayInputs() {
        tvTodayWorkedValue.text = "00h 00m"
        tvDurationValue.text = "00h 00m"
        tvBreakValue.text = "0m"
        tvCoreHoursBadge.visibility = View.GONE
    }

    private fun showTimePicker(initialTime: LocalTime, onTimeSelected: (LocalTime) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialTime.hour)
            .setMinute(initialTime.minute)
            .setTitleText("Select Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSelected(LocalTime.of(picker.hour, picker.minute))
        }
        picker.show(childFragmentManager, "time_picker")
    }

    private fun LocalTime.formatTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
