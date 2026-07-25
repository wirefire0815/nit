package dev.whitefire.nit.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkTimeConfig
import dev.whitefire.nit.ui.history.HistoryFragment
import dev.whitefire.nit.ui.settings.SettingsFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private var onboardingDialogShown = false

    private val viewModel: MainViewModel by viewModels {
        val app = application as NitApplication
        MainViewModelFactory(app.workDayRepository, app.preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)

        setupViewPager()
        setupBottomNav()
        observeOnboarding()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_main
                    1 -> bottomNav.selectedItemId = R.id.nav_history
                    2 -> bottomNav.selectedItemId = R.id.nav_settings
                }
            }
        })
    }

    private fun setupBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_history -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_settings -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }

    private fun observeOnboarding() {
        (application as NitApplication).preferencesRepository.let { repo ->
            kotlinx.coroutines.MainScope().launch {
                repo.onboardingCompletedFlow.collect { completed ->
                    if (!completed && !onboardingDialogShown) {
                        showOnboardingWizard()
                    }
                }
            }
        }
    }

    private fun showOnboardingWizard() {
        onboardingDialogShown = true
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_onboarding, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etOnboardingTargetHours)
        val switchCoreHours = dialogView.findViewById<MaterialSwitch>(R.id.switchOnboardingCoreHours)
        val btnGetStarted = dialogView.findViewById<Button>(R.id.btnOnboardingGetStarted)
        val toggleFriday = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupOnboardingFriday)

        toggleFriday.check(R.id.btnOnboardingEarlyFriday)

        btnGetStarted.setOnClickListener {
            val target = etTarget.text?.toString()?.toFloatOrNull() ?: 38.5f
            val coreHoursEnabled = switchCoreHours.isChecked
            val plannerMode = if (toggleFriday.checkedButtonId == R.id.btnOnboardingEarlyFriday) {
                WorkTimeConfig.SchedulePlannerMode.MIN_CORE_HOURS
            } else {
                WorkTimeConfig.SchedulePlannerMode.BALANCED
            }

            val currentConfig = viewModel.workTimeConfig.value ?: WorkTimeConfig()
            val newConfig = currentConfig.copy(
                weeklyTargetHours = target,
                enableCoreHours = coreHoursEnabled,
                plannerMode = plannerMode
            )
            (application as NitApplication).preferencesRepository.run {
                GlobalScope.launch {
                    setWorkTimeConfig(newConfig)
                    setOnboardingCompleted(true)
                }
            }
            viewModel.completeOnboarding()
            dialog.dismiss()
        }

        dialog.show()
    }
}

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> HistoryFragment()
            2 -> SettingsFragment()
            else -> HomeFragment()
        }
    }
}
