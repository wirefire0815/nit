package dev.whitefire.nit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.whitefire.nit.NitApplication
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkDay
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalHours: TextView
    private lateinit var tvDaysWorked: TextView

    private val viewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as NitApplication
        HistoryViewModelFactory(app.workDayRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        tvTotalHours = view.findViewById(R.id.tvTotalHours)
        tvDaysWorked = view.findViewById(R.id.tvDaysWorked)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = WorkDayAdapter(
            onDeleteClick = { workDay ->
                viewModel.deleteWorkDay(workDay)
            }
        )
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.workDays.collect { workDays ->
                        (recyclerView.adapter as? WorkDayAdapter)?.submitList(workDays)
                        updateStats(workDays)
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

    private fun updateStats(workDays: List<WorkDay>) {
        val stats = viewModel.getWeekStats(workDays)
        val totalMins = Math.round(stats.totalHours * 60)
        tvTotalHours.text = String.format("%02dh %02dm", totalMins / 60, totalMins % 60)
        tvDaysWorked.text = "${stats.daysWorked} days"
    }
}
