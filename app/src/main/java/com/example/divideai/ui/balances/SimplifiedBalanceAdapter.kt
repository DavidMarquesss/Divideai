package com.example.divideai.ui.balances

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.databinding.ItemSimplifiedBalanceBinding
import java.text.NumberFormat
import java.util.Locale

class SimplifiedBalanceAdapter(
    @ColorRes private val amountColorRes: Int
) : ListAdapter<SimplifiedBalanceItem, SimplifiedBalanceAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSimplifiedBalanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemSimplifiedBalanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        fun bind(item: SimplifiedBalanceItem) {
            val context = binding.root.context
            binding.tvName.text = item.otherUserName
            binding.tvAmount.text = formatter.format(item.amount)
            binding.tvAmount.setTextColor(ContextCompat.getColor(context, amountColorRes))
            binding.ivAvatar.loadUserAvatar(item.otherUserId)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SimplifiedBalanceItem>() {
            override fun areItemsTheSame(a: SimplifiedBalanceItem, b: SimplifiedBalanceItem) =
                a.otherUserId == b.otherUserId
            override fun areContentsTheSame(a: SimplifiedBalanceItem, b: SimplifiedBalanceItem) = a == b
        }
    }
}
