package dev.whitefire.noedap.domain.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Represents a single work day with start, end, and break information
 */
data class WorkDay(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val breakMinutes: Int = 0,
    val notes: String = "",
    val createdAt: java.time.Instant = java.time.Instant.now(),
    val updatedAt: java.time.Instant = java.time.Instant.now()
) {
    val dayOfWeek: DayOfWeek
        get() = date.dayOfWeek
    
    /**
     * Gross duration (without breaks)
     */
    val grossDuration: Duration?
        get() {
            if (startTime == null || endTime == null) return null
            val duration = Duration.between(startTime, endTime)
            return if (duration.isNegative) null else duration
        }
    
    /**
     * Net duration (with breaks subtracted)
     */
    val netDuration: Duration?
        get() {
            val gross = grossDuration ?: return null
            return gross.minusMinutes(breakMinutes.toLong())
        }
    
    /**
     * Effective work hours (net duration in hours)
     */
    val effectiveHours: Float
        get() {
            val net = netDuration ?: return 0f
            return net.toMinutes().toFloat() / 60f
        }
    
    /**
     * Check if this is a complete work day (has start and end time)
     */
    val isComplete: Boolean
        get() = startTime != null && endTime != null
    
    /**
     * Check if work day is within Kernzeit
     */
    fun isInKernzeit(config: WorkTimeConfig = DEFAULT_WORK_CONFIG): Boolean {
        val coreTime = config.coreTimes[dayOfWeek] ?: return false
        if (coreTime.start == null || coreTime.end == null) return false
        
        val actualStart = startTime ?: return false
        val actualEnd = endTime ?: return false
        
        return !actualStart.isBefore(coreTime.start) && 
               !actualEnd.isAfter(coreTime.end)
    }
    
    /**
     * Calculate required break minutes based on gross duration and break rules
     */
    fun calculateRequiredBreak(config: WorkTimeConfig = DEFAULT_WORK_CONFIG): Int {
        val gross = grossDuration ?: return 0
        val hours = gross.toMinutes().toFloat() / 60f
        
        var totalBreak = 0
        for (rule in config.breakRules) {
            if (hours >= rule.afterHours) {
                totalBreak += (rule.durationHours * 60).toInt()
            }
        }
        return totalBreak
    }
    
    /**
     * Check if required breaks are satisfied
     */
    fun hasSufficientBreaks(config: WorkTimeConfig = DEFAULT_WORK_CONFIG): Boolean {
        return breakMinutes >= calculateRequiredBreak(config)
    }
    
    /**
     * Get display string for the date
     */
    fun getDateDisplay(): String {
        return when (date) {
            LocalDate.now() -> "Today"
            LocalDate.now().minusDays(1) -> "Yesterday"
            LocalDate.now().plusDays(1) -> "Tomorrow"
            else -> date.dayOfWeek.toString().take(3) + ", " + date.toString()
        }
    }
    
    /**
     * Get formatted duration string
     */
    fun getDurationString(): String {
        val gross = grossDuration ?: return "00:00"
        val net = netDuration ?: return "00:00"
        
        val grossHours = gross.toHours()
        val grossMinutes = gross.toMinutesPart()
        val netHours = net.toHours()
        val netMinutes = net.toMinutesPart()
        
        return if (breakMinutes > 0) {
            String.format("%02d:%02d (net: %02d:%02d)", 
                grossHours, grossMinutes, netHours, netMinutes)
        } else {
            String.format("%02d:%02d", grossHours, grossMinutes)
        }
    }
}
