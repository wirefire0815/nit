package dev.whitefire.noedap

import android.app.Application
import dev.whitefire.noedap.data.local.AppDatabase
import dev.whitefire.noedap.data.repository.UserPreferencesRepository
import dev.whitefire.noedap.data.repository.WorkDayRepository

/**
 * Custom Application class for Noedap
 */
class NoedapApplication : Application() {
    
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
    
    val workDayRepository: WorkDayRepository by lazy {
        WorkDayRepository.getInstance(database.workDayDao())
    }
    
    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository.getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
}
