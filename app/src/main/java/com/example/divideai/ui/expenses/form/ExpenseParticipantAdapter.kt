package com.example.divideai.ui.expenses.form

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.model.Member
import com.example.divideai.databinding.ItemParticipantsBinding

class ExpenseParticipantsAdapter(
    private val onParticipantToggled: (String) -> Unit
) : ListAdapter<Member, ExpenseParticipantsAdapter.ViewHolder>(DiffCallback()) {

    private var selectedIds = setOf<String>()

    fun updateSelectedIds(ids: Set<String>) {
        selectedIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParticipantsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemParticipantsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member: Member) {
            binding.checkbox.visibility = View.VISIBLE
            binding.tvStatusLabelPayed.visibility = View.GONE
            binding.ivCheckPago.visibility = View.GONE
            binding.tvNomeParticipante.text = member.name.ifEmpty { member.email }
            binding.checkbox.isChecked = selectedIds.contains(member.id)

            binding.root.setOnClickListener { onParticipantToggled(member.id) }
            binding.checkbox.setOnClickListener { onParticipantToggled(member.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Member, newItem: Member) = oldItem == newItem
    }
}