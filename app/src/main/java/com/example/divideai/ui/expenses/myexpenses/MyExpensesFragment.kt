package com.example.divideai.ui.expenses.myexpenses

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.R
import com.example.divideai.databinding.FragmentMyExpensesBinding
import com.example.divideai.ui.expenses.ExpensesAdapter
import com.example.divideai.ui.expenses.details.ExpenseDetailsActivity
import java.text.NumberFormat
import java.util.Locale

import kotlinx.coroutines.launch

class MyExpensesFragment : Fragment() {

    private var _binding: FragmentMyExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyExpensesViewModel
    private lateinit var adapter: ExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MyExpensesViewModel::class.java)
        _binding = FragmentMyExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToggleGroup()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ExpensesAdapter(
            onItemClicked = { expense ->
                val intent = Intent(context, ExpenseDetailsActivity::class.java).apply {
                    putExtra("EXPENSE_ID", expense.id)
                }
                startActivity(intent)
            },
            onPayClicked = { expenseId ->
                viewModel.markAsPaid(expenseId)
            }
        )
        binding.rvDespesas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDespesas.adapter = adapter
    }

    private fun setupToggleGroup() {
        binding.toggleGroupFiltro.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val filter = when (checkedId) {
                    R.id.btnOpcao1 -> ExpenseFilter.ALL
                    R.id.btnOpcao2 -> ExpenseFilter.PAID
                    R.id.btnOpcao3 -> ExpenseFilter.UNPAID
                    else -> ExpenseFilter.ALL
                }
                viewModel.setFilter(filter)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.expenses)
                    
                    // Atualiza o texto de Total a Pagar
                    val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
                    binding.valueExpenses.text = formatador.format(state.totalToPay)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarrega os dados do Firebase toda vez que a aba for focada pelo usuário
        viewModel.loadExpenses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}