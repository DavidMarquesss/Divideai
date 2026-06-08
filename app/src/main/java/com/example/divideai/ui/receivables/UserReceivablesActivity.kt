package com.example.divideai.ui.receivables

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divideai.R
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.databinding.ActivityUserReceivablesBinding
import com.example.divideai.ui.expenses.details.ExpenseDetailsActivity

class UserReceivablesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserReceivablesBinding
    private val viewModel: UserReceivablesViewModel by viewModels()
    private lateinit var adapter: UserExpenseAdapter

    private lateinit var debtorId: String
    private lateinit var debtorName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserReceivablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        debtorId = intent.getStringExtra("DEBTOR_ID") ?: ""
        debtorName = intent.getStringExtra("DEBTOR_NAME") ?: getString(R.string.default_user)

        if (debtorId.isEmpty()) {
            Toast.makeText(this, R.string.error_invalid_debtor, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvUserName.text = debtorName
        binding.ivAvatar.loadUserAvatar(debtorId)

        setupRecyclerView()
        setupListeners()
        setupObservers()

        viewModel.loadUserExpenses(debtorId, debtorName)
    }

    private fun setupRecyclerView() {
        adapter = UserExpenseAdapter { expenseId ->
            val intent = Intent(this, ExpenseDetailsActivity::class.java).apply {
                putExtra("EXPENSE_ID", expenseId)
            }
            startActivity(intent)
        }
        binding.rvUserExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvUserExpenses.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.userExpenses.observe(this) { expenses ->
            adapter.submitList(expenses)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
