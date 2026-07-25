package dev.whitefire.nit.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import dev.whitefire.nit.R
import dev.whitefire.nit.domain.model.WorkDay

class WeeklyGroupAdapter(
    private val onDeleteClick: (WorkDay) -> Unit
) : ListAdapter<HistoryViewModel.WeeklyHistoryGroup, WeeklyGroupViewHolder>(WeeklyGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_week_group, parent, false)
        return WeeklyGroupViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: WeeklyGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class WeeklyGroupViewHolder(
    itemView: View,
    private val onDeleteClick: (WorkDay) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val tvWeekTitle: TextView = itemView.findViewById(R.id.tvWeekTitle)
    private val tvWeekTotalBadge: TextView = itemView.findViewById(R.id.tvWeekTotalBadge)
    private val tvWeekSubtext: TextView = itemView.findViewById(R.id.tvWeekSubtext)
    private val daysContainer: LinearLayout = itemView.findViewById(R.id.daysContainer)

    fun bind(group: HistoryViewModel.WeeklyHistoryGroup) {
        tvWeekTitle.text = group.getFormattedTitle()
        tvWeekTotalBadge.text = group.getFormattedTotalHours()
        tvWeekSubtext.text = "${group.daysWorked} days logged this week"

        daysContainer.removeAllViews()
        val inflater = LayoutInflater.from(itemView.context)

        group.days.forEach { day ->
            val dayView = inflater.inflate(R.layout.item_work_day, daysContainer, false)
            val tvDate: TextView = dayView.findViewById(R.id.tvDate)
            val tvDuration: TextView = dayView.findViewById(R.id.tvDuration)
            val tvNotes: TextView = dayView.findViewById(R.id.tvNotes)
            val btnDelete: MaterialButton = dayView.findViewById(R.id.btnDelete)

            tvDate.text = day.getDateDisplay()
            val netMins = day.netDuration?.toMinutes()?.toInt() ?: 0
            tvDuration.text = String.format("Net: %02dh %02dm", netMins / 60, netMins % 60)
            tvNotes.text = day.notes.ifEmpty { "No notes" }

            btnDelete.setOnClickListener { onDeleteClick(day) }
            daysContainer.addView(dayView)
        }
    }
}

class WeeklyGroupDiffCallback : DiffUtil.ItemCallback<HistoryViewModel.WeeklyHistoryGroup>() {
    override fun areItemsTheSame(oldItem: HistoryViewModel.WeeklyHistoryGroup, newItem: HistoryViewModel.WeeklyHistoryGroup): Boolean {
        return oldItem.startDate == newItem.startDate
    }

    override fun areContentsTheSame(oldItem: HistoryViewModel.WeeklyHistoryGroup, newItem: HistoryViewModel.WeeklyHistoryGroup): Boolean {
        return oldItem == newItem
    }
}
