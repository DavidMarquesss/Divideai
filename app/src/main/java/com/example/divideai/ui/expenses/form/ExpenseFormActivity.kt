package com.example.divideai.ui.expenses.form

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivityExpenseFormBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExpenseFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseFormBinding
    private val viewModel: ExpenseFormViewModel by viewModels()
    private lateinit var adapter: ExpenseParticipantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupId = intent.getStringExtra("GROUP_ID")

        if (groupId == null) {
            Toast.makeText(this, "Erro: Grupo não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupListeners(groupId)
        setupObservers()

        viewModel.loadGroupData(groupId)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseParticipantsAdapter { memberId ->
            viewModel.toggleParticipant(memberId)
        }
        binding.rvParticipantes.adapter = adapter
    }

    private fun setupListeners(groupId: String) {
        binding.btnClose.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString()
            val desc = binding.inputDescription.text.toString()
            val amount = binding.inputAmount.text.toString()

            viewModel.saveExpense(groupId, title, desc, amount)
        }

        binding.btnEditPagador.setOnClickListener {
            val members = viewModel.allMembers.value ?: return@setOnClickListener

            val memberNames = members.map { it.email }.toTypedArray()

            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.expense_form_who_paid_title))
                .setItems(memberNames) { _, which ->
                    viewModel.setPayer(members[which])
                }
                .show()
        }
    }

    private fun setupObservers() {
        viewModel.saveStatus.observe(this) { (success, errorMessage) ->
            if (success) {
                Toast.makeText(this, "Despesa criada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.availableMembers.observe(this) { members ->
            adapter.submitList(members)
        }

        viewModel.selectedParticipantIds.observe(this) { selectedIds ->
            adapter.updateSelectedIds(selectedIds)
        }

        viewModel.selectedPayer.observe(this) { payer ->
            payer?.let {
                binding.layoutPagador.tvNomeUsuario.text = it.name.ifEmpty { it.email }
            }
        }
    }
}