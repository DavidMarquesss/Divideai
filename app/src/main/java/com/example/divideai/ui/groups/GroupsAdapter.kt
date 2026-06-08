package com.example.divideai.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.model.Group
import com.example.divideai.databinding.ItemGroupBinding

class GroupsAdapter(
    private val onItemClick: (Group) -> Unit,
    private val onLongClick: (Group) -> Unit
) : ListAdapter<Group, GroupsAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private var isSelectionMode = false
    private var selectedIds = setOf<String>()

    fun updateSelectionState(isSelectionMode: Boolean, selectedIds: Set<String>) {
        this.isSelectionMode = isSelectionMode
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Group) {
            binding.txtGroupTitle.text = item.title
            binding.txtGroupDesc.text = item.description

            val bmp = Base64Image.decode(item.imageBase64)
            if (bmp != null) {
                binding.imgGroupPhoto.setImageBitmap(bmp)
                binding.imgGroupPhoto.visibility = View.VISIBLE
                binding.imgGroupIcon.visibility = View.GONE
            } else {
                binding.imgGroupPhoto.visibility = View.GONE
                binding.imgGroupPhoto.setImageDrawable(null)
                binding.imgGroupIcon.visibility = View.VISIBLE
            }

            if (isSelectionMode) {
                binding.checkbox.visibility = View.VISIBLE
                binding.checkbox.isChecked = selectedIds.contains(item.id)
                binding.root.setOnClickListener { onItemClick(item) }
                binding.checkbox.setOnClickListener { onItemClick(item) }
            } else {
                binding.checkbox.visibility = View.GONE
                // abrir detalhes
                binding.root.setOnClickListener { onItemClick(item) }
            }

            // Clique Longo inicia a seleção
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }
}

