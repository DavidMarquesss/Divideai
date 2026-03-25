package com.example.divideai.ui.groups.expenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.divideai.R
import com.example.divideai.databinding.FragmentGroupExpensesBinding
import com.example.divideai.ui.expenses.details.ExpenseDetailsActivity
import com.example.divideai.ui.expenses.form.ExpenseFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GroupExpensesFragment : Fragment() {

    private var _binding: FragmentGroupExpensesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupExpensesViewModel by viewModels()
    private var groupId: String = ""
    private lateinit var adapter: ExpensesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = requireActivity().intent.getStringExtra("GROUP_ID") ?: ""
        val currentUserId = viewModel.getCurrentUserId()

        adapter = ExpensesAdapter(
            currentUserId = currentUserId,
            onItemClick = { expense ->
                if (viewModel.isSelectionMode.value == true) {
                    viewModel.toggleSelection(expense.id)
                } else {
                    val intent = Intent(context, ExpenseDetailsActivity::class.java).apply {
                        putExtra("EXPENSE_ID", expense.id)
                    }
                    startActivity(intent)
                }
            },
            onLongClick = { expense ->
                if (viewModel.isSelectionMode.value == false) {
                    viewModel.setSelectionMode(true)
                    viewModel.toggleSelection(expense.id)
                }
            },
            onPayClick = { expense ->
                viewModel.markAsPaid(expense, currentUserId)
            }
        )

        setupRecyclerView()
        setupObservers()
        setupListeners()
        handleBackPress()
    }

    override fun onResume() {
        super.onResume()
        if (groupId.isNotEmpty()) viewModel.fetchExpenses(groupId)
    }

    private fun setupRecyclerView() {
        binding.rvExpenses.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.expenseList.observe(viewLifecycleOwner) { expenses ->
            adapter.submitList(expenses)
        }


        viewModel.isSelectionMode.observe(viewLifecycleOwner) { isSelectionMode ->
            updateUIForSelectionMode(isSelectionMode)
            adapter.updateSelectionState(isSelectionMode, viewModel.selectedIds.value ?: emptySet())
        }


        viewModel.selectedIds.observe(viewLifecycleOwner) { selectedIds ->
            adapter.updateSelectionState(viewModel.isSelectionMode.value ?: false, selectedIds)
            val totalItems = viewModel.expenseList.value?.size ?: 0
            binding.chkSelectAll.isChecked = totalItems > 0 && selectedIds.size == totalItems
        }


        viewModel.deleteStatus.observe(viewLifecycleOwner) { status ->
            status?.let { (success, message) ->
                if (success) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
                viewModel.clearDeleteStatus()
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(context, ExpenseFormActivity::class.java).apply {
                putExtra("GROUP_ID", groupId)
            }
            startActivity(intent)
        }

        binding.chkSelectAll.setOnClickListener {
            viewModel.toggleSelectAll(binding.chkSelectAll.isChecked)
        }

        binding.btnDeleteSelected.setOnClickListener {
            val count = viewModel.selectedIds.value?.size ?: 0

            if (count > 0) {
                val plural = if (count > 1) "despesas" else "despesa"

                val message = resources.getQuantityString(R.plurals.dialog_delete_expenses_message, count, count)

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_confirmation_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.dialog_confirm) { dialog, _ ->
                        viewModel.deleteSelectedExpenses(groupId)
                    }
                    .show()
            } else {
                Toast.makeText(requireContext(), R.string.error_select_something_to_delete, Toast.LENGTH_SHORT).show()
            }
        }

        binding.inputSearch.addTextChangedListener { text ->
            viewModel.filterExpenses(text.toString())
        }
    }


    private fun updateUIForSelectionMode(isSelectionMode: Boolean) {
        if (isSelectionMode) {
            binding.btnAddExpense.visibility = View.GONE
            binding.layoutSelection.visibility = View.VISIBLE
        } else {
            binding.btnAddExpense.visibility = View.VISIBLE
            binding.layoutSelection.visibility = View.GONE
            binding.chkSelectAll.isChecked = false
        }
    }


    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.isSelectionMode.value == true) {
                    viewModel.setSelectionMode(false)
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}