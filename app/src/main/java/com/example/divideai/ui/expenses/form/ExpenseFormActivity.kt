package com.example.divideai.ui.expenses.form

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.image.loadUserAvatar
import com.example.divideai.data.model.ExpenseCategory
import com.example.divideai.databinding.ActivityExpenseFormBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExpenseFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseFormBinding
    private val viewModel: ExpenseFormViewModel by viewModels()
    private lateinit var adapter: ExpenseParticipantsAdapter
    private var receiptBase64: String = ""

    private val pickReceiptLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) handlePickedReceipt(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupId = intent.getStringExtra("GROUP_ID")

        if (groupId == null) {
            Toast.makeText(this, R.string.error_group_not_found_form, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupCategoryChips()
        setupReceiptControls()
        setupListeners(groupId)
        setupObservers()

        viewModel.loadGroupData(groupId)
    }

    private fun setupReceiptControls() {
        binding.btnAddReceipt.setOnClickListener { onReceiptButtonClicked() }
        binding.ivReceiptThumb.setOnClickListener { onReceiptButtonClicked() }
    }

    private fun onReceiptButtonClicked() {
        if (receiptBase64.isEmpty()) {
            launchReceiptPicker()
        } else {
            showReceiptOptions()
        }
    }

    private fun launchReceiptPicker() {
        pickReceiptLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showReceiptOptions() {
        val options = arrayOf(
            getString(R.string.expense_receipt_replace),
            getString(R.string.expense_receipt_remove)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.expense_receipt_options_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchReceiptPicker()
                    1 -> clearReceipt()
                }
            }
            .show()
    }

    private fun handlePickedReceipt(uri: Uri) {
        val encoded = Base64Image.encodeFromUri(
            context = this,
            uri = uri,
            maxSize = Base64Image.SIZE_RECEIPT
        )
        if (encoded == null) {
            Toast.makeText(this, R.string.edit_profile_photo_load_error, Toast.LENGTH_SHORT).show()
            return
        }
        receiptBase64 = encoded
        Base64Image.decode(encoded)?.let {
            binding.ivReceiptThumb.setImageBitmap(it)
            binding.ivReceiptThumb.imageTintList = null
            binding.ivReceiptThumb.setBackgroundColor(Color.TRANSPARENT)
        }
        binding.btnAddReceipt.text = getString(R.string.expense_receipt_replace)
    }

    private fun clearReceipt() {
        receiptBase64 = ""
        binding.ivReceiptThumb.setImageResource(R.drawable.ic_image_24dp)
        binding.ivReceiptThumb.imageTintList =
            android.content.res.ColorStateList.valueOf(getColor(R.color.grafite))
        binding.ivReceiptThumb.setBackgroundResource(R.color.light_gray)
        binding.btnAddReceipt.text = getString(R.string.expense_receipt_add)
    }

    /**
     * Cria dinamicamente um [Chip] para cada [ExpenseCategory] e marca o atual
     * como selecionado. A escolha é propagada para o [ExpenseFormViewModel].
     */
    private fun setupCategoryChips() {
        val current = viewModel.selectedCategory.value ?: ExpenseCategory.DEFAULT
        ExpenseCategory.entries.forEach { category ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = getString(category.labelRes)
                isCheckable = true
                isChecked = category == current
                setChipIconResource(category.iconRes)
                isChipIconVisible = true
                tag = category
            }
            binding.chipGroupCategory.addView(chip)
        }
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedId) ?: return@setOnCheckedStateChangeListener
            (chip.tag as? ExpenseCategory)?.let { viewModel.setCategory(it) }
        }
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

            viewModel.saveExpense(groupId, title, desc, amount, receiptBase64)
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
                Toast.makeText(this, R.string.expense_created_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_prefix, errorMessage), Toast.LENGTH_LONG).show()
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
                binding.layoutPagador.ivAvatar.loadUserAvatar(it.userId)
            }
        }
    }
}