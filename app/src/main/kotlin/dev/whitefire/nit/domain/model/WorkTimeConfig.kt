package dev.whitefire.nit.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Configuration for working time rules
 */
data class WorkTimeConfig(
    /** Target hours per week */
    val weeklyTargetHours: Float = 38.5f,
    
    /** Core times (Kernzeiten) per day of week */
    val coreTimes: Map<DayOfWeek, CoreTime> = mapOf(
        DayOfWeek.MONDAY to CoreTime(LocalTime.of(9, 30), LocalTime.of(16, 0)),
        DayOfWeek.TUESDAY to CoreTime(LocalTime.of(9, 30), LocalTime.of(16, 0)),
        DayOfWeek.WEDNESDAY to CoreTime(LocalTime.of(9, 30), LocalTime.of(16, 0)),
        DayOfWeek.THURSDAY to CoreTime(LocalTime.of(9, 30), LocalTime.of(16, 0)),
        DayOfWeek.FRIDAY to CoreTime(LocalTime.of(9, 30), LocalTime.of(12, 30)),
        DayOfWeek.SATURDAY to CoreTime(null, null),
        DayOfWeek.SUNDAY to CoreTime(null, null)
    ),
    
    /** Break rules: break duration after X hours of work */
    val breakRules: List<BreakRule> = listOf(
        BreakRule(6.0f, 0.5f) // 30 min break after 6 hours
    )
) {
    data class CoreTime(
        val start: LocalTime?, 
        val end: LocalTime?
    )
    
    data class BreakRule(
        val afterHours: Float,
        val durationHours: Float
    )
}

/**
 * Default configuration for Neuron Automation interns
 */
val DEFAULT_WORK_CONFIG = WorkTimeConfig()
