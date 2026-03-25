package com.example.divideai.ui.groups.form

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivityGroupFormBinding

class GroupFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupFormBinding
    private val viewModel: GroupFormViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()

        val groupId = intent.getStringExtra("GROUP_ID")
        if (groupId != null) {
            viewModel.loadGroupForEdit(groupId)
        }

        binding.btnUpload.setOnClickListener {
            Toast.makeText(this, getString(R.string.not_implemented_upload), Toast.LENGTH_SHORT).show()
        }

        binding.btnRemove.setOnClickListener {
            Toast.makeText(this, getString(R.string.not_implemented_remove), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString()
            val desc = binding.inputDescription.text.toString()
            viewModel.saveGroup(title, desc)
        }
    }

    private fun setupObservers() {

        viewModel.saveStatus.observe(this) { (success, errorMessage) ->
            if (success) {
                val msg = if (intent.hasExtra("GROUP_ID")) {
                    getString(R.string.group_updated_success)
                } else {
                    getString(R.string.group_created_success)
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            } else {
                val errorMsg = getString(R.string.error_prefix, errorMessage)
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.groupToEdit.observe(this) { group ->
            group?.let {
                binding.inputTitle.setText(it.title)
                binding.inputDescription.setText(it.description)
            }
        }
    }
}