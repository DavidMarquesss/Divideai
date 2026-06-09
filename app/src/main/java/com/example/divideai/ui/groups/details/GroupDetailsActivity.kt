package com.example.divideai.ui.groups.details

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.invite.GroupInviteCode
import com.example.divideai.databinding.ActivityGroupDetailsBinding
import com.example.divideai.databinding.DialogGroupQrBinding
import com.example.divideai.ui.groups.form.GroupFormActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

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

        binding.btnShareQr.setOnClickListener {
            val groupId = intent.getStringExtra("GROUP_ID") ?: return@setOnClickListener
            showInviteQrDialog(groupId)
        }
    }

    /**
     * Renderiza um QR Code com o convite [GroupInviteCode.encode] e exibe num
     * dialog. O outro usuário escaneia pelo botão de scan no [com.example.divideai.ui.groups.GroupsFragment].
     */
    private fun showInviteQrDialog(groupId: String) {
        val dialogBinding = DialogGroupQrBinding.inflate(layoutInflater)
        val bitmap = BarcodeEncoder().encodeBitmap(
            GroupInviteCode.encode(groupId),
            BarcodeFormat.QR_CODE,
            640,
            640
        )
        dialogBinding.ivQrCode.setImageBitmap(bitmap)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.group_qr_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setupObservers() {
        viewModel.group.observe(this) { group ->
            if (group != null) {
                binding.txtGroupTitle.text = group.title
                binding.txtGroupDescription.text = group.description

                val bmp = Base64Image.decode(group.photo)
                if (bmp != null) {
                    binding.imgGroupPhoto.setImageBitmap(bmp)
                    binding.imgGroupPhoto.visibility = View.VISIBLE
                    binding.imgGroupPlaceholder.visibility = View.GONE
                } else {
                    binding.imgGroupPhoto.visibility = View.GONE
                    binding.imgGroupPlaceholder.visibility = View.VISIBLE
                }
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