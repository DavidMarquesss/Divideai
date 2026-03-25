package com.example.divideai.ui.groups.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.R
import com.example.divideai.data.model.Expense
import com.example.divideai.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.Locale

class ExpensesAdapter(
    private val currentUserId: String,
    private val onItemClick: (Expense) -> Unit,
    private val onLongClick: (Expense) -> Unit,
    private val onPayClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpensesAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    private var isSelectionMode = false
    private var selectedIds = setOf<String>()

    fun updateSelectionState(isSelectionMode: Boolean, selectedIds: Set<String>) {
        this.isSelectionMode = isSelectionMode
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Expense) {
            binding.tvTitulo.text = item.title
            binding.tvData.text = item.date

            setupPaymentStatus(item)

            if (isSelectionMode) {
                binding.checkbox.visibility = View.VISIBLE
                binding.checkbox.isChecked = selectedIds.contains(item.id)
                binding.root.setOnClickListener { onItemClick(item) }
                binding.checkbox.setOnClickListener { onItemClick(item) }

                // Esconde os status
                binding.btnPagar.visibility = View.GONE
                binding.tvStatusLabelToPay.visibility = View.GONE
                binding.tvStatusLabelPayed.visibility = View.GONE
                binding.ivCheckPago.visibility = View.GONE
            } else {
                binding.checkbox.visibility = View.GONE
                binding.root.setOnClickListener { onItemClick(item) }
            }

            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun setupPaymentStatus(expense: Expense) {
            val context = binding.root.context
            val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())

            binding.tvStatusLabelToPay.visibility = View.GONE
            binding.btnPagar.visibility = View.GONE
            binding.tvStatusLabelPayed.visibility = View.GONE
            binding.ivCheckPago.visibility = View.GONE

            if (expense.payerId == currentUserId) {
                // você pagou a conta

                val allPaid = expense.participants.all { it.paid }

                if (allPaid && expense.participants.isNotEmpty()) {
                    // Todos já pagaram a você
                    binding.tvStatusLabelPayed.text = context.getString(R.string.status_all_paid)
                    binding.tvStatusLabelPayed.visibility = View.VISIBLE
                    binding.ivCheckPago.visibility = View.VISIBLE
                } else {
                    // Ainda têm pessoas te devendo
                    binding.tvStatusLabelToPay.text = context.getString(R.string.status_to_receive)
                    binding.tvStatusLabelToPay.visibility = View.VISIBLE
                }

            } else {
                // outra pessoa pagou a conta

                val myShare = expense.participants.find { it.userId == currentUserId }

                if (myShare != null) {
                    if (myShare.paid) {
                        // Você devia, mas já pagou
                        binding.tvStatusLabelPayed.text = context.getString(R.string.status_paid)
                        binding.tvStatusLabelPayed.visibility = View.VISIBLE
                        binding.ivCheckPago.visibility = View.VISIBLE
                    } else {
                        // Você está devendo
                        val amountFormatted = formatador.format(myShare.amountOwed)
                        binding.tvStatusLabelToPay.text = context.getString(R.string.status_you_owe, amountFormatted)
                        binding.tvStatusLabelToPay.visibility = View.VISIBLE
                        binding.btnPagar.visibility = View.VISIBLE

                        binding.btnPagar.setOnClickListener {
                            onPayClick(expense)
                        }
                    }
                } else {
                    // Você não foi incluído na divisão dessa conta específica
                    binding.tvStatusLabelPayed.text = context.getString(R.string.status_not_involved)
                    binding.tvStatusLabelPayed.visibility = View.VISIBLE
                }
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}