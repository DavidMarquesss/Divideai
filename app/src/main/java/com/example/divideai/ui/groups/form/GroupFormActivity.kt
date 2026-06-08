package com.example.divideai.ui.groups.form

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.databinding.ActivityGroupFormBinding

class GroupFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupFormBinding
    private val viewModel: GroupFormViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) handlePickedImage(uri)
    }

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

        binding.btnUpload.setOnClickListener { launchPicker() }
        binding.groupImg.setOnClickListener { launchPicker() }
        binding.btnRemove.setOnClickListener { clearImage() }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString()
            val desc = binding.inputDescription.text.toString()
            viewModel.saveGroup(title, desc)
        }
    }

    private fun launchPicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun handlePickedImage(uri: Uri) {
        val encoded = Base64Image.encodeFromUri(
            context = this,
            uri = uri,
            maxSize = Base64Image.SIZE_AVATAR
        )
        if (encoded == null) {
            Toast.makeText(this, R.string.edit_profile_photo_load_error, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.setImage(encoded)
        showPhoto(encoded)
    }

    private fun clearImage() {
        viewModel.setImage("")
        binding.imgGroupPhoto.visibility = View.GONE
        binding.imgGroupPlaceholder.visibility = View.VISIBLE
    }

    private fun showPhoto(base64: String) {
        val bmp = Base64Image.decode(base64) ?: return
        binding.imgGroupPhoto.setImageBitmap(bmp)
        binding.imgGroupPhoto.visibility = View.VISIBLE
        binding.imgGroupPlaceholder.visibility = View.GONE
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
                if (it.imageBase64.isNotEmpty()) showPhoto(it.imageBase64)
            }
        }
    }
}
