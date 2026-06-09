package com.example.divideai.ui.expenses.details

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.data.model.ExpenseCategory
import com.example.divideai.databinding.ActivityExpenseDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

class ExpenseDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailsBinding
    private val viewModel: ExpenseDetailsViewModel by viewModels()
    private val adapter = ExpenseDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenseId = intent.getStringExtra("EXPENSE_ID")
        if (expenseId == null) {
            Toast.makeText(this, R.string.error_loading_expense, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupObservers()

        // Inicia a busca dos dados
        viewModel.loadExpenseDetails(expenseId)
    }

    private fun setupUI() {
        binding.btnClose.setOnClickListener { finish() }

        binding.rvParticipantes.layoutManager = LinearLayoutManager(this)
        binding.rvParticipantes.adapter = adapter
    }

    private fun setupObservers() {
        val formatador = NumberFormat.getCurrencyInstance(Locale.getDefault())

        viewModel.expenseData.observe(this) { expense ->
            binding.tvTitulo.text = getString(R.string.title_label, expense?.title)
            binding.tvDescricao.text = getString(R.string.description_label, expense?.description)
            binding.tvValor.text = getString(R.string.amount_label, formatador.format(expense?.amount))
            val category = ExpenseCategory.fromId(expense?.category)
            binding.ivCategory.setImageResource(category.iconRes)
            binding.tvCategory.text = getString(category.labelRes)
            expense?.payerId?.let { binding.layoutPagador.ivAvatar.loadUserAvatar(it) }

            renderReceipt(expense?.receipt)
        }

        viewModel.payerName.observe(this) { nomePagador ->
            binding.layoutPagador.tvNomeUsuario.text = nomePagador
        }

        viewModel.participantsList.observe(this) { list ->
            adapter.submitList(list)
        }

        viewModel.progressPercent.observe(this) { percent ->
            binding.minhaProgressBar.progress = percent
        }

        viewModel.summaryText.observe(this) { text ->
            binding.tvResumoValores.text = text
        }
    }

    private fun renderReceipt(base64: String?) {
        val bmp = Base64Image.decode(base64)
        if (bmp == null) {
            binding.ivReceipt.visibility = View.GONE
            binding.tvRotuloComprovante.visibility = View.GONE
            return
        }
        binding.ivReceipt.setImageBitmap(bmp)
        binding.ivReceipt.visibility = View.VISIBLE
        binding.tvRotuloComprovante.visibility = View.VISIBLE
        binding.ivReceipt.setOnClickListener { showReceiptFullScreen(bmp) }
    }

    private fun showReceiptFullScreen(bmp: android.graphics.Bitmap) {
        val fullImage = ImageView(this).apply {
            setImageBitmap(bmp)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.expense_receipt_view_title)
            .setView(fullImage)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}