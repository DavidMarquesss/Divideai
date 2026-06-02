package com.example.divideai.ui.receivables

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.R
import com.example.divideai.data.model.ExpenseCategory
import com.example.divideai.data.model.ReceivableItem
import com.example.divideai.databinding.ItemUserExpenseBinding
import java.text.NumberFormat
import java.util.Locale

class UserExpenseAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<ReceivableItem, UserExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemUserExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemUserExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReceivableItem) {
            val context = binding.root.context
            binding.tvExpenseTitle.text = item.title
            binding.tvDate.text = item.date
            binding.ivCategory.setImageResource(ExpenseCategory.fromId(item.category).iconRes)
            val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.tvAmount.text = formatador.format(item.amountOwed)

            if (item.isPaid) {
                binding.tvAmount.setTextColor(Color.parseColor("#388E3C")) // sage_green
                binding.tvStatusLabel.setTextColor(Color.parseColor("#388E3C"))
                binding.tvStatusLabel.text = context.getString(R.string.status_paid)
                binding.ivIcon.setColorFilter(Color.parseColor("#388E3C"))
                binding.ivIcon.setImageResource(R.drawable.icon_check_24dp)
            } else {
                binding.tvAmount.setTextColor(Color.parseColor("#D32F2F")) // strawberry_red
                binding.tvStatusLabel.setTextColor(Color.parseColor("#D32F2F"))
                binding.tvStatusLabel.text = context.getString(R.string.valores_a_receber)
                binding.ivIcon.setColorFilter(Color.parseColor("#D32F2F"))
                binding.ivIcon.setImageResource(R.drawable.ic_trending_down_24dp)
            }

            binding.root.setOnClickListener {
                onItemClick(item.expenseId)
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<ReceivableItem>() {
        override fun areItemsTheSame(oldItem: ReceivableItem, newItem: ReceivableItem): Boolean {
            return oldItem.expenseId == newItem.expenseId && oldItem.debtorId == newItem.debtorId
        }

        override fun areContentsTheSame(oldItem: ReceivableItem, newItem: ReceivableItem): Boolean {
            return oldItem == newItem
        }
    }
}
