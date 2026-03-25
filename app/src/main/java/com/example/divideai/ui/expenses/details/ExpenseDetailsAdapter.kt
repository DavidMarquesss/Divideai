package com.example.divideai.ui.expenses.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.divideai.R
import com.example.divideai.data.model.ParticipantDetail
import com.example.divideai.databinding.ItemParticipantsBinding
import java.text.NumberFormat
import java.util.Locale

class ExpenseDetailsAdapter : ListAdapter<ParticipantDetail, ExpenseDetailsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemParticipantsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemParticipantsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ParticipantDetail) {
            val context = binding.root.context
            val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())
            val valorFormatado = formatador.format(item.amountOwed)

            // Esconde o checkbox nesta tela
            binding.checkbox.visibility = View.GONE

            // Mostra o nome e o valor que a pessoa devia
            val displayName = item.name.ifEmpty { item.email }
            binding.tvNomeParticipante.text = "$displayName ($valorFormatado)"

            binding.tvStatusLabelPayed.visibility = View.VISIBLE

            if (item.paid) {
                binding.tvStatusLabelPayed.text = context.getString(R.string.status_paid)
                binding.tvStatusLabelPayed.setTextColor(ContextCompat.getColor(context, R.color.sage_green))
                binding.ivCheckPago.visibility = View.VISIBLE
            } else {
                binding.tvStatusLabelPayed.text = context.getString(R.string.status_to_pay)
                binding.tvStatusLabelPayed.setTextColor(ContextCompat.getColor(context, R.color.strawberry_red)) // ou outra cor de alerta
                binding.ivCheckPago.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ParticipantDetail>() {
        override fun areItemsTheSame(oldItem: ParticipantDetail, newItem: ParticipantDetail) = oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: ParticipantDetail, newItem: ParticipantDetail) = oldItem == newItem
    }
}