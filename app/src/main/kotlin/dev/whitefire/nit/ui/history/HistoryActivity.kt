package dev.whitefire.nit.ui.history

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalHours: TextView
    private lateinit var tvDaysWorked: TextView

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as NitApplication).workDayRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        tvTotalHours = findViewById(R.id.tvTotalHours)
        tvDaysWorked = findViewById(R.id.tvDaysWorked)

        setupRecyclerView()
        setupObservers()

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WeeklyGroupAdapter(
            onDeleteClick = { workDay ->
                viewModel.deleteWorkDay(workDay)
            }
        )
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.weeklyGroups.collect { groups ->
                        (recyclerView.adapter as? WeeklyGroupAdapter)?.submitList(groups)
                        updateHeaderStats(groups)
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun updateHeaderStats(groups: List<HistoryViewModel.WeeklyHistoryGroup>) {
        val totalHours = groups.sumOf { it.totalHours.toDouble() }.toFloat()
        val totalDays = groups.sumOf { it.daysWorked }
        val totalMins = Math.round(totalHours * 60)

        tvTotalHours.text = String.format("%02dh %02dm", totalMins / 60, totalMins % 60)
        tvDaysWorked.text = "$totalDays days"
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHistory()
    }
}
