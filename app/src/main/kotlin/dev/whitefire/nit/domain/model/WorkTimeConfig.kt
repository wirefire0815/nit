package dev.whitefire.nit.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Configuration for working time rules
 */
data class WorkTimeConfig(
    /** Target hours per week */
    val weeklyTargetHours: Float = 38.5f,
    
    /** Whether Kernzeiten (core hours) rules are enabled */
    val enableKernzeiten: Boolean = true,

    /** Strategy for Friday finish: EARLY_KERNZEIT, BALANCED, or CUSTOM */
    val fridayTargetMode: FridayExitMode = FridayExitMode.EARLY_KERNZEIT,

    /** Custom net hours target for Friday when in CUSTOM mode */
    val customFridayTargetHours: Float = 3.0f,
    
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
    enum class FridayExitMode {
        EARLY_KERNZEIT, // Leave right after Friday core hours (3h 00m net)
        BALANCED,       // Spread remaining hours equally including Friday
        CUSTOM          // Custom target hours on Friday
    }

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
