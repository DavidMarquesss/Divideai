package com.example.divideai.ui.groups.details

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivityGroupDetailsBinding
import com.example.divideai.ui.groups.form.GroupFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private val viewModel: GroupDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
        setupObservers()

        val groupId = intent.getStringExtra("GROUP_ID")

        if (!groupId.isNullOrEmpty()) {
            viewModel.loadGroup(groupId)
        } else {
            Toast.makeText(this, R.string.error_loading_group, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val groupId = intent.getStringExtra("GROUP_ID")

            val intent = Intent(this, GroupFormActivity::class.java).apply {
                putExtra("GROUP_ID", groupId)
            }
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_group_title)
                .setMessage(R.string.dialog_delete_group_message)
                .setNegativeButton(R.string.dialog_cancel, null) // Não faz nada se cancelar
                .setPositiveButton(R.string.dialog_delete) { _, _ ->
                    val groupId = intent.getStringExtra("GROUP_ID")
                    if (!groupId.isNullOrEmpty()) {
                        viewModel.deleteGroup(groupId)
                    }
                }
                .show()
        }
    }

    private fun setupObservers() {
        viewModel.group.observe(this) { group ->
            if (group != null) {
                binding.txtGroupTitle.text = group.title
                binding.txtGroupDescription.text = group.description
            } else {
                Toast.makeText(this, R.string.error_group_not_found, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.deleteStatus.observe(this) { status ->
            status?.let { (success, errorMessage) ->
                if (success) {
                    Toast.makeText(this, R.string.success_group_deleted, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMsg = getString(R.string.error_deleting_group, errorMessage)
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                }

                viewModel.clearDeleteStatus()
            }
        }
    }
}