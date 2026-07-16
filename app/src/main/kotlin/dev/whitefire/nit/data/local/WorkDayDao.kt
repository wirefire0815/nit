package dev.whitefire.nit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import java.time.LocalDate

/**
 * Data Access Object for WorkDay entities
 */
@Dao
interface WorkDayDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workDay: WorkDayEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(workDays: List<WorkDayEntity>)
    
    @Query("SELECT * FROM work_days WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: LocalDate): WorkDayEntity?
    
    @Query("SELECT * FROM work_days WHERE date BETWEEN :start AND :end")
    suspend fun getByDateRange(start: LocalDate, end: LocalDate): List<WorkDayEntity>
    
    @Query("SELECT * FROM work_days ORDER BY date DESC")
    suspend fun getAll(): List<WorkDayEntity>
    
    @Query("SELECT * FROM work_days ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 20): List<WorkDayEntity>
    
    @Query("SELECT * FROM work_days WHERE date >= :start AND date <= :end ORDER BY date ASC")
    suspend fun getByWeek(start: LocalDate, end: LocalDate): List<WorkDayEntity>
    
    @Query("SELECT * FROM work_days WHERE date >= :start ORDER BY date ASC LIMIT :limit")
    suspend fun getFromDate(start: LocalDate, limit: Int = 100): List<WorkDayEntity>
    
    @Query("DELETE FROM work_days WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM work_days WHERE date = :date")
    suspend fun deleteByDate(date: LocalDate)
    
    @Query("DELETE FROM work_days")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM work_days")
    suspend fun count(): Int
    
    @Query("SELECT COUNT(*) FROM work_days WHERE date = :date")
    suspend fun exists(date: LocalDate): Int
    
    @Transaction
    suspend fun upsert(workDay: WorkDayEntity) {
        insert(workDay)
    }
}
