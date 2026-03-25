package com.example.divideai.ui.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.R
import com.example.divideai.databinding.ItemExpenseBinding
import com.example.divideai.ui.expenses.myexpenses.MyExpenseUiModel
import java.text.NumberFormat
import java.util.Locale

class ExpensesAdapter(
    private val onItemClicked: (MyExpenseUiModel) -> Unit,
    private val onPayClicked: (String) -> Unit
) : ListAdapter<MyExpenseUiModel, ExpensesAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(expense: MyExpenseUiModel) {
            val context = binding.root.context
            binding.tvTitulo.text = expense.title
            binding.tvData.text = expense.date

            val myParticipant = expense.participants.find { it.id == "u1" }
            val hasPaidMyShare = expense.isPaidByMe || (myParticipant?.hasPaid == true)

            if (hasPaidMyShare) {
                // Estado Pago
                binding.btnPagar.visibility = View.GONE
                binding.tvStatusLabelToPay.visibility = View.GONE
                
                binding.ivCheckPago.visibility = View.VISIBLE
                binding.tvStatusLabelPayed.visibility = View.VISIBLE
                binding.tvStatusLabelPayed.text = context.getString(R.string.payed)
            } else {
                // Estado Não Pago
                binding.ivCheckPago.visibility = View.GONE
                binding.tvStatusLabelPayed.visibility = View.GONE
                
                binding.btnPagar.visibility = View.VISIBLE
                binding.tvStatusLabelToPay.visibility = View.VISIBLE
                // Encontra quanto Você deve
                val owed = myParticipant?.amountOwed ?: 0.0
                val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
                val owedFormatted = formatador.format(owed)
                binding.tvStatusLabelToPay.text = context.getString(R.string.status_you_owe, owedFormatted)
                
                binding.btnPagar.setOnClickListener {
                    onPayClicked(expense.id)
                }
            }

            binding.root.setOnClickListener {
                onItemClicked(expense)
            }
        }
    }
}

class ExpenseDiffCallback : DiffUtil.ItemCallback<MyExpenseUiModel>() {
    override fun areItemsTheSame(oldItem: MyExpenseUiModel, newItem: MyExpenseUiModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MyExpenseUiModel, newItem: MyExpenseUiModel): Boolean {
        return oldItem == newItem
    }
}