package dev.whitefire.nit.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class WorkTimeConfig(
    /** Target hours per week */
    val weeklyTargetHours: Float = 38.5f,
    
    /** Whether mandatory core hours rules are enabled */
    val enableCoreHours: Boolean = true,

    /** Strategy for schedule planning across remaining days */
    val plannerMode: SchedulePlannerMode = SchedulePlannerMode.BALANCED,

    /** Custom net hours target for specified focus days */
    val customTargetHours: Float = 3.0f,
    
    /** Core times per day of week */
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
        BreakRule(6.0f, 0.5f)
    )
) {
    enum class SchedulePlannerMode {
        BALANCED,       // Evenly distribute remaining target hours across remaining work days
        MIN_CORE_HOURS, // Minimize hours on short/core days (e.g. Friday core hours)
        CUSTOM          // Custom user-defined target allocation
    }

    // Aliases for compatibility
    val enableKernzeiten: Boolean get() = enableCoreHours
    val fridayTargetMode: SchedulePlannerMode get() = plannerMode
    val customFridayTargetHours: Float get() = customTargetHours

    data class CoreTime(
        val start: LocalTime?, 
        val end: LocalTime?
    )
    
    data class BreakRule(
        val afterHours: Float,
        val durationHours: Float
    )
}

val DEFAULT_WORK_CONFIG = WorkTimeConfig()
