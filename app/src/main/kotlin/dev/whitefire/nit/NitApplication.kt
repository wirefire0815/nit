package dev.whitefire.nit

import android.app.Application
import dev.whitefire.nit.data.local.AppDatabase
import dev.whitefire.nit.data.repository.UserPreferencesRepository
import dev.whitefire.nit.data.repository.WorkDayRepository

/**
 * Custom Application class for nit
 */
class NitApplication : Application() {
    
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
