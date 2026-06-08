package com.example.divideai.ui.receivables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.data.model.ReceivableUserItem
import com.example.divideai.databinding.ItemReceivableUserBinding

class ReceivablesUserAdapter(
    private val onItemClick: (String, String) -> Unit
) : ListAdapter<ReceivableUserItem, ReceivablesUserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemReceivableUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemReceivableUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReceivableUserItem) {
            binding.tvDebtorName.text = item.debtorName
            binding.ivAvatar.loadUserAvatar(item.debtorId)

            binding.root.setOnClickListener {
                onItemClick(item.debtorId, item.debtorName)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<ReceivableUserItem>() {
        override fun areItemsTheSame(oldItem: ReceivableUserItem, newItem: ReceivableUserItem): Boolean {
            return oldItem.debtorId == newItem.debtorId
        }

        override fun areContentsTheSame(oldItem: ReceivableUserItem, newItem: ReceivableUserItem): Boolean {
            return oldItem == newItem
        }
    }
}
