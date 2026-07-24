package dev.whitefire.nit.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Represents a work week with its days and statistics
 */
data class WorkWeek(
    val startDate: LocalDate,
    val workDays: List<WorkDay> = emptyList(),
    val config: WorkTimeConfig = DEFAULT_WORK_CONFIG
) {
    val endDate: LocalDate
        get() = startDate.plusDays(6) // Monday + 6 days = Sunday
    
    val weekNumber: Int
        get() {
            val weekFields = WeekFields.of(Locale.getDefault())
            return startDate.get(weekFields.weekOfWeekBasedYear())
        }
    
    val year: Int
        get() = startDate.year
    
    /**
     * Get work days for a specific day of week
     */
    fun getWorkDays(dayOfWeek: DayOfWeek): List<WorkDay> {
        return workDays.filter { it.dayOfWeek == dayOfWeek }
    }
    
    /**
     * Get work day for a specific date
     */
    fun getWorkDay(date: LocalDate): WorkDay? {
        return workDays.firstOrNull { it.date == date }
    }
    
    /**
     * Total gross hours worked this week
     */
    val totalGrossHours: Float
        get() = workDays.sumOf { day ->
            (day.grossDuration?.toMinutes()?.toFloat()?.div(60f) ?: 0f).toDouble()
        }.toFloat()
    
    /**
     * Total net hours worked this week (with breaks)
     */
    val totalNetHours: Float
        get() = workDays.sumOf { it.effectiveHours.toDouble() }.toFloat()
    
    /**
     * Remaining hours to reach target
     */
    val remainingHours: Float
        get() = config.weeklyTargetHours - totalNetHours
    
    /**
     * Check if week target is met
     */
    val isTargetMet: Boolean
        get() = totalNetHours >= config.weeklyTargetHours
    
    /**
     * Get progress percentage (0-100)
     */
    val progressPercentage: Float
        get() = (totalNetHours / config.weeklyTargetHours * 100f).coerceAtMost(100f)
    
    /**
     * Check if week is complete (all required days worked)
     */
    val isComplete: Boolean
        get() = isTargetMet && workDays.all { day ->
            day.startTime != null && day.endTime != null
        }
    
    /**
     * Get display string for the week
     */
    fun getDisplayRange(): String {
        val start = startDate
        val end = endDate
        return if (start.year == end.year) {
            "${start.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${start.dayOfMonth} - ${end.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${end.dayOfMonth}, ${start.year}"
        } else {
            "${start.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${start.dayOfMonth}, ${start.year} - ${end.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${end.dayOfMonth}, ${end.year}"
        }
    }
    
    /**
     * Get work days sorted by date
     */
    fun getSortedWorkDays(): List<WorkDay> {
        return workDays.sortedBy { it.date }
    }
    
    /**
     * Add or update a work day
     */
    fun withWorkDay(workDay: WorkDay): WorkWeek {
        val existingIndex = workDays.indexOfFirst { it.date == workDay.date }
        val newDays = workDays.toMutableList()
        
        if (existingIndex >= 0) {
            newDays[existingIndex] = workDay
        } else {
            newDays.add(workDay)
        }
        
        return copy(workDays = newDays)
    }
    
    /**
     * Remove a work day
     */
    fun withoutWorkDay(date: LocalDate): WorkWeek {
        return copy(workDays = workDays.filter { it.date != date })
    }
    
    /**
     * Data class holding weekly projection details
     */
    data class ScheduleProjection(
        val todayTargetHours: Float,
        val fridayTargetHours: Float,
        val nonFridayDailyTargetHours: Float,
        val remainingDaysCount: Int,
        val summaryText: String
    )

    /**
     * Calculate smart schedule projections for remaining work days in the week
     */
    fun calculateScheduleProjection(currentDate: LocalDate = LocalDate.now()): ScheduleProjection {
        val weekMonToFri = (0..4).map { startDate.plusDays(it.toLong()) }
        
        // Sum completed hours for days strictly prior to currentDate
        val pastDaysCompleted = weekMonToFri.filter { it.isBefore(currentDate) }
        val pastHours = pastDaysCompleted.sumOf { date ->
            getWorkDay(date)?.effectiveHours?.toDouble() ?: 0.0
        }.toFloat()

        val totalRemaining = (config.weeklyTargetHours - pastHours).coerceAtLeast(0f)

        val remainingWorkDays = weekMonToFri.filter { !it.isBefore(currentDate) }
        if (remainingWorkDays.isEmpty()) {
            return ScheduleProjection(0f, 0f, 0f, 0, "Week target complete!")
        }

        fun formatHours(h: Float): String {
            val totalMins = Math.round(h * 60)
            val hrs = totalMins / 60
            val mins = totalMins % 60
            return String.format("%dh %02dmin", hrs, mins)
        }

        val containsFriday = remainingWorkDays.contains(startDate.plusDays(4))

        val (todayTarget, friTarget, nonFriTarget, summary) = when (config.plannerMode) {
            WorkTimeConfig.SchedulePlannerMode.BALANCED -> {
                val dailyAvg = totalRemaining / remainingWorkDays.size
                val text = "Work ${formatHours(dailyAvg)} daily across remaining ${remainingWorkDays.size} day(s) to hit ${config.weeklyTargetHours}h target."
                Tuple4(dailyAvg, dailyAvg, dailyAvg, text)
            }
            WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS -> {
                val friCore = config.coreTimes[DayOfWeek.FRIDAY]
                val fTarget = if (containsFriday && friCore?.start != null && friCore.end != null) {
                    java.time.Duration.between(friCore.start, friCore.end).toMinutes().toFloat() / 60f
                } else 3.0f

                val nonFriCount = remainingWorkDays.count { it.dayOfWeek != DayOfWeek.FRIDAY }
                val nfTarget = if (nonFriCount > 0) {
                    (totalRemaining - fTarget).coerceAtLeast(0f) / nonFriCount
                } else 0f

                val tTarget = if (currentDate.dayOfWeek == DayOfWeek.FRIDAY) fTarget else nfTarget
                val text = if (currentDate.dayOfWeek == DayOfWeek.FRIDAY) {
                    "Friday target: ${formatHours(tTarget)} to finish weekly goal of ${config.weeklyTargetHours}h."
                } else if (containsFriday && nonFriCount > 0) {
                    "Work ${formatHours(nfTarget)} today & remaining days to finish core hours on Friday (${formatHours(fTarget)})."
                } else {
                    "Suggested target: ${formatHours(tTarget)} for remaining ${remainingWorkDays.size} day(s)."
                }
                Tuple4(tTarget, fTarget, nfTarget, text)
            }
            WorkTimeConfig.SchedulePlannerMode.CUSTOM -> {
                val cFri = config.customTargetHours
                val nonFriCount = remainingWorkDays.count { it.dayOfWeek != DayOfWeek.FRIDAY }
                val nfTarget = if (nonFriCount > 0) {
                    (totalRemaining - cFri).coerceAtLeast(0f) / nonFriCount
                } else 0f
                val tTarget = if (currentDate.dayOfWeek == DayOfWeek.FRIDAY) cFri else nfTarget
                val text = "Work ${formatHours(tTarget)} today (${remainingWorkDays.size} day(s) remaining)."
                Tuple4(tTarget, cFri, nfTarget, text)
            }
        }

        return ScheduleProjection(
            todayTargetHours = todayTarget,
            fridayTargetHours = friTarget,
            nonFridayDailyTargetHours = nonFriTarget,
            remainingDaysCount = remainingWorkDays.size,
            summaryText = summary
        )
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
    
    companion object {
        /**
         * Create a WorkWeek for the current week
         */
        fun currentWeek(config: WorkTimeConfig = DEFAULT_WORK_CONFIG): WorkWeek {
            val today = LocalDate.now()
            val weekFields = WeekFields.of(Locale.getDefault())
            val weekNumber = today.get(weekFields.weekOfWeekBasedYear())
            // Find the Monday of the current week
            val startDate = today.minusDays(today.dayOfWeek.value.toLong() - DayOfWeek.MONDAY.value.toLong())
            return WorkWeek(startDate, emptyList(), config)
        }
        
        /**
         * Create a WorkWeek from a date
         */
        fun fromDate(date: LocalDate, config: WorkTimeConfig = DEFAULT_WORK_CONFIG): WorkWeek {
            val weekFields = WeekFields.of(Locale.getDefault())
            // Find the Monday of the week containing this date
            val startDate = date.minusDays(date.dayOfWeek.value.toLong() - DayOfWeek.MONDAY.value.toLong())
            return WorkWeek(startDate, emptyList(), config)
        }
        
        /**
         * Create a WorkWeek from year and week number
         */
        fun fromYearWeek(year: Int, week: Int, config: WorkTimeConfig = DEFAULT_WORK_CONFIG): WorkWeek? {
            return try {
                val weekFields = WeekFields.of(Locale.getDefault())
                // Create a date for the first day of the year
                val firstDay = LocalDate.of(year, 1, 1)
                // Find the first Monday of the year
                val firstMonday = firstDay.plusDays(((DayOfWeek.MONDAY.value - firstDay.dayOfWeek.value + 7) % 7).toLong())
                // Add (week-1) weeks to get to the start of the desired week
                val startDate = firstMonday.plusWeeks((week - 1).toLong())
                WorkWeek(startDate, emptyList(), config)
            } catch (e: Exception) {
                null
            }
        }
    }
}
