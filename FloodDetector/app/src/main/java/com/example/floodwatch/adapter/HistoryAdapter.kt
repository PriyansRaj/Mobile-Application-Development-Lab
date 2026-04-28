package com.example.floodwatch.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.floodwatch.R
import com.example.floodwatch.databinding.ItemHistoryBinding
import com.example.floodwatch.db.AnalysisRecord
import com.example.floodwatch.util.ImageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (AnalysisRecord) -> Unit,
    private val onItemLongClick: (AnalysisRecord) -> Unit
) : ListAdapter<AnalysisRecord, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: AnalysisRecord) {
            binding.apply {
                tvTitle.text = record.title
                tvDate.text = formatDate(record.timestampMs)
                tvFloodPercent.text = String.format("%.1f%%", record.floodPercent)

                chipSeverity.text = record.severity
                chipSeverity.setChipBackgroundColorResource(getSeverityColor(record.severity))

                val thumbnail = ImageUtils.loadFromInternalStorage(record.originalImagePath)
                if (thumbnail != null) {
                    ivThumbnail.setImageBitmap(thumbnail)
                } else {
                    ivThumbnail.setImageResource(R.drawable.ic_history)
                }

                root.setOnClickListener { onItemClick(record) }
                root.setOnLongClickListener {
                    onItemLongClick(record)
                    true
                }
            }
        }

        private fun formatDate(timestampMs: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
            return sdf.format(Date(timestampMs))
        }

        private fun getSeverityColor(severity: String): Int {
            return when (severity) {
                "Severe" -> R.color.severity_severe
                "Moderate" -> R.color.severity_moderate
                "Low" -> R.color.severity_low
                else -> R.color.severity_low
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AnalysisRecord>() {
        override fun areItemsTheSame(oldItem: AnalysisRecord, newItem: AnalysisRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AnalysisRecord, newItem: AnalysisRecord): Boolean {
            return oldItem == newItem
        }
    }
}
