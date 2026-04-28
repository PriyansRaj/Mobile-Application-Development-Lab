package com.example.floodwatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.floodwatch.adapter.HistoryAdapter
import com.example.floodwatch.databinding.FragmentHistoryBinding
import com.example.floodwatch.db.AnalysisRecord
import com.example.floodwatch.viewmodel.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupChips()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onItemClick = { record ->
                showRecordDetails(record)
            },
            onItemLongClick = { record ->
                showDeleteConfirmation(record)
            }
        )
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.searchBar.doAfterTextChanged { text ->
            viewModel.search(text?.toString() ?: "")
        }
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(binding.chipAll.id) -> viewModel.clearFilter()
                checkedIds.contains(binding.chipSevere.id) -> viewModel.filterBySeverity("Severe")
                checkedIds.contains(binding.chipModerate.id) -> viewModel.filterBySeverity("Moderate")
                checkedIds.contains(binding.chipLow.id) -> viewModel.filterBySeverity("Low")
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.analyses.collect { records ->
                    adapter.submitList(records)
                    binding.emptyState.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvHistory.visibility = if (records.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showRecordDetails(record: AnalysisRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(record.title)
            .setMessage("Flood: ${record.floodPercent}%\nSeverity: ${record.severity}\nFlooded Pixels: ${record.floodedPixels}/${record.totalPixels}")
            .setPositiveButton("OK", null)
            .setNegativeButton("Delete") { _, _ ->
                showDeleteConfirmation(record)
            }
            .show()
    }

    private fun showDeleteConfirmation(record: AnalysisRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete \"${record.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteRecord(record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
