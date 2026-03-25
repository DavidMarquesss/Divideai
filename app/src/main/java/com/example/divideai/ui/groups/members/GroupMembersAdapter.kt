package com.example.divideai.ui.groups.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.model.Member
import com.example.divideai.databinding.ItemMemberBinding

class MembersAdapter(
    private val onItemClick: (Member) -> Unit,
    private val onLongClick: (Member) -> Unit
) : ListAdapter<Member, MembersAdapter.MemberViewHolder>(MemberDiffCallback()) {

    private var isSelectionMode = false
    private var selectedIds = setOf<String>()

    fun updateSelectionState(isSelectionMode: Boolean, selectedIds: Set<String>) {
        this.isSelectionMode = isSelectionMode
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Member) {
            binding.tvEmailMembro.text = item.name.ifEmpty { item.email }

            if (isSelectionMode) {
                binding.checkbox.visibility = View.VISIBLE
                binding.checkbox.isChecked = selectedIds.contains(item.id)
                binding.root.setOnClickListener { onItemClick(item) }
                binding.checkbox.setOnClickListener { onItemClick(item) }
            } else {
                binding.checkbox.visibility = View.GONE
                binding.root.setOnClickListener { onItemClick(item) }
            }

            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    class MemberDiffCallback : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Member, newItem: Member) = oldItem == newItem
    }
}