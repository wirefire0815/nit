package dev.whitefire.nit.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class WorkCalculationTest {

    @Test
    fun testUserScenario_MondayAndTuesdayWorked_BalancedProjection() {
        val config = WorkTimeConfig(
            weeklyTargetHours = 38.5f,
            plannerMode = WorkTimeConfig.SchedulePlannerMode.BALANCED
        )

        val mondayDate = LocalDate.of(2026, 7, 20)
        val tuesdayDate = LocalDate.of(2026, 7, 21)
        val wednesdayDate = LocalDate.of(2026, 7, 22)

        val mon = WorkDay(
            date = mondayDate,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 33),
            breakMinutes = 30
        )

        val tue = WorkDay(
            date = tuesdayDate,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 12),
            breakMinutes = 30
        )

        val workWeek = WorkWeek(startDate = mondayDate, config = config)
            .withWorkDay(mon)
            .withWorkDay(tue)

        val projection = workWeek.calculateScheduleProjection(wednesdayDate)

        // Mon net = 15.75h worked. Remaining = 22.75h across 3 days (Wed, Thu, Fri) = 7.5833h/day (~7h 35m).
        assertEquals(7.5833f, projection.todayTargetHours, 0.01f)
        assertEquals(3, projection.remainingDaysCount)
    }

    @Test
    fun testUserScenario_MondayAndTuesdayWorked_MinCoreHoursProjection() {
        val config = WorkTimeConfig(
            weeklyTargetHours = 38.5f,
            plannerMode = WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS
        )

        val mondayDate = LocalDate.of(2026, 7, 20)
        val tuesdayDate = LocalDate.of(2026, 7, 21)
        val wednesdayDate = LocalDate.of(2026, 7, 22)

        val mon = WorkDay(
            date = mondayDate,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 33),
            breakMinutes = 30
        )

        val tue = WorkDay(
            date = tuesdayDate,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 12),
            breakMinutes = 30
        )

        val workWeek = WorkWeek(startDate = mondayDate, config = config)
            .withWorkDay(mon)
            .withWorkDay(tue)

        val projection = workWeek.calculateScheduleProjection(wednesdayDate)

        // Remaining 22.75h - 3h Fri core = 19.75h / 2 days (Wed, Thu) = 9.875h/day (9h 53m).
        assertEquals(3.0f, projection.fridayTargetHours, 0.01f)
        assertEquals(9.875f, projection.todayTargetHours, 0.01f)
    }

    @Test
    fun testCoreHoursCheck_CorrectValidation() {
        val config = WorkTimeConfig(enableCoreHours = true)

        val validDay = WorkDay(
            date = LocalDate.of(2026, 7, 20),
            startTime = LocalTime.of(9, 15),
            endTime = LocalTime.of(16, 30)
        )
        assertTrue(validDay.satisfiesCoreHours(config))

        val lateDay = WorkDay(
            date = LocalDate.of(2026, 7, 20),
            startTime = LocalTime.of(9, 45),
            endTime = LocalTime.of(16, 30)
        )
        assertFalse(lateDay.satisfiesCoreHours(config))

        val disabledConfig = config.copy(enableCoreHours = false)
        assertTrue(lateDay.satisfiesCoreHours(disabledConfig))
    }
}
