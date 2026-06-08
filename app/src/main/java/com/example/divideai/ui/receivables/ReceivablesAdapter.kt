package com.example.divideai.ui.receivables

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.data.model.ReceivableItem
import com.example.divideai.databinding.ItemReceivableBinding
import java.text.NumberFormat
import java.util.Locale

class ReceivablesAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<ReceivableItem, ReceivablesAdapter.ReceivableViewHolder>(ReceivableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivableViewHolder {
        val binding = ItemReceivableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReceivableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReceivableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReceivableViewHolder(private val binding: ItemReceivableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReceivableItem) {
            binding.tvExpenseTitle.text = item.title
            binding.tvDate.text = item.date
            binding.tvDebtorName.text = item.debtorName
            binding.ivAvatar.loadUserAvatar(item.debtorId)
            val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.tvAmount.text = formatador.format(item.amountOwed)

            if (item.isPaid) {
                binding.tvStatus.text = "Pago"
                binding.tvStatus.setTextColor(Color.parseColor("#388E3C")) // sage_green logic
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            } else {
                binding.tvStatus.text = "Pendente"
                binding.tvStatus.setTextColor(Color.parseColor("#D32F2F")) // red logic
                binding.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
            }

            binding.root.setOnClickListener {
                onItemClick(item.expenseId)
            }
        }
    }

    class ReceivableDiffCallback : DiffUtil.ItemCallback<ReceivableItem>() {
        override fun areItemsTheSame(oldItem: ReceivableItem, newItem: ReceivableItem): Boolean {
            return oldItem.expenseId == newItem.expenseId && oldItem.debtorId == newItem.debtorId
        }

        override fun areContentsTheSame(oldItem: ReceivableItem, newItem: ReceivableItem): Boolean {
            return oldItem == newItem
        }
    }
}
