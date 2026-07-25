package dev.whitefire.nit.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whitefire.nit.data.repository.WorkDayRepository
import dev.whitefire.nit.domain.model.WorkDay
import dev.whitefire.nit.domain.model.WorkWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

class HistoryViewModel(
    private val workDayRepository: WorkDayRepository
) : ViewModel() {

    private val _weeklyGroups = MutableStateFlow<List<WeeklyHistoryGroup>>(emptyList())
    val weeklyGroups: StateFlow<List<WeeklyHistoryGroup>> = _weeklyGroups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val days = workDayRepository.getRecentWorkDays(100)
            
            val groups = days.groupBy { WorkWeek.fromDate(it.date).startDate }
                .map { (weekStart, weekDays) ->
                    val weekEnd = weekStart.plusDays(6)
                    val weekNumber = weekStart.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                    val totalHours = weekDays.sumOf { it.effectiveHours.toDouble() }.toFloat()
                    val daysWorked = weekDays.count { it.isComplete }
                    
                    WeeklyHistoryGroup(
                        startDate = weekStart,
                        endDate = weekEnd,
                        weekNumber = weekNumber,
                        totalHours = totalHours,
                        daysWorked = daysWorked,
                        days = weekDays.sortedBy { it.date }
                    )
                }
                .sortedByDescending { it.startDate }

            _weeklyGroups.value = groups
            _isLoading.value = false
        }
    }

    fun deleteWorkDay(workDay: WorkDay) {
        viewModelScope.launch {
            workDayRepository.deleteWorkDayById(workDay.id)
            loadHistory()
        }
    }

    data class WeeklyHistoryGroup(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val weekNumber: Int,
        val totalHours: Float,
        val daysWorked: Int,
        val days: List<WorkDay>
    ) {
        fun getFormattedTitle(): String {
            val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
            return "Week $weekNumber • ${startDate.format(fmt)} – ${endDate.format(fmt)}"
        }

        fun getFormattedTotalHours(): String {
            val totalMins = Math.round(totalHours * 60)
            return String.format("%02dh %02dm", totalMins / 60, totalMins % 60)
        }
    }
}

class HistoryViewModelFactory(
    private val workDayRepository: WorkDayRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(workDayRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
