package dev.whitefire.noedap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room entity for WorkDay
 * Uses String representations for date/time for Room compatibility
 */
@Entity(tableName = "work_days")
@TypeConverters(Converters::class)
data class WorkDayEntity(
    @PrimaryKey
    val id: String,
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val breakMinutes: Int = 0,
    val notes: String = "",
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
)
