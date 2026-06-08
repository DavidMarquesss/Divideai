package com.example.divideai.ui.dashboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.databinding.ItemCategoryBreakdownBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryBreakdownAdapter :
    ListAdapter<CategoryBreakdownItem, CategoryBreakdownAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBreakdownBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemCategoryBreakdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        fun bind(item: CategoryBreakdownItem) {
            val context = binding.root.context
            binding.colorSwatch.backgroundTintList = ColorStateList.valueOf(item.colorArgb)
            binding.ivIcon.setImageResource(item.category.iconRes)
            binding.tvLabel.text = context.getString(item.category.labelRes)
            binding.tvAmount.text = formatter.format(item.total)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CategoryBreakdownItem>() {
            override fun areItemsTheSame(a: CategoryBreakdownItem, b: CategoryBreakdownItem) =
                a.category == b.category
            override fun areContentsTheSame(a: CategoryBreakdownItem, b: CategoryBreakdownItem) = a == b
        }
    }
}
