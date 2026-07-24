package dev.whitefire.nit.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class WorkCalculationTest {

    @Test
    fun testUserScenario_MondayAndTuesdayWorked_FridayEarlyOutProjection() {
        val config = WorkTimeConfig(
            weeklyTargetHours = 38.5f,
            enableKernzeiten = true,
            fridayTargetMode = WorkTimeConfig.FridayExitMode.EARLY_KERNZEIT
        )

        // Monday start week: Jul 20, 2026 (Mon) to Jul 26, 2026 (Sun)
        val mondayDate = LocalDate.of(2026, 7, 20)
        val tuesdayDate = LocalDate.of(2026, 7, 21)
        val wednesdayDate = LocalDate.of(2026, 7, 22)

        // Monday: 8h 03m net work (e.g. 08:00 - 16:33 with 30m break)
        val mon = WorkDay(
            date = mondayDate,
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(16, 33),
            breakMinutes = 30
        )

        // Tuesday: 7h 42m net work (e.g. 08:00 - 16:12 with 30m break)
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

        // Mon net = 483 mins (8h 03m), Tue net = 462 mins (7h 42m), Total = 945 mins (15h 45m)
        // Target = 2310 mins (38.5h), Remaining = 1365 mins (22h 45m)
        // Friday Early Kernzeit Target = 180 mins (3h 00m)
        // Wed & Thu Remaining = 1185 mins across 2 days -> 592.5 mins/day = 9h 52.5m (9h 53m)
        assertEquals(3.0f, projection.fridayTargetHours, 0.01f)
        assertEquals(9.875f, projection.todayTargetHours, 0.01f)
        assertEquals(9.875f, projection.nonFridayDailyTargetHours, 0.01f)
    }

    @Test
    fun testKernzeitCheck_CorrectValidation() {
        val config = WorkTimeConfig(enableKernzeiten = true)

        // Valid Mon Kernzeit (arrived by 09:30, stayed until 16:00)
        val validDay = WorkDay(
            date = LocalDate.of(2026, 7, 20),
            startTime = LocalTime.of(9, 15),
            endTime = LocalTime.of(16, 30)
        )
        assertTrue(validDay.isInKernzeit(config))

        // Late arrival (09:45 > 09:30) -> Violation
        val lateDay = WorkDay(
            date = LocalDate.of(2026, 7, 20),
            startTime = LocalTime.of(9, 45),
            endTime = LocalTime.of(16, 30)
        )
        assertFalse(lateDay.isInKernzeit(config))

        // Kernzeiten disabled -> Always passes
        val disabledConfig = config.copy(enableKernzeiten = false)
        assertTrue(lateDay.isInKernzeit(disabledConfig))
    }
}
