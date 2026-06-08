package com.example.divideai.ui.profile

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.R
import com.example.divideai.data.image.Base64Image
import com.example.divideai.data.image.UserAvatarCache
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.UserRepository
import com.example.divideai.databinding.ActivityEditProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private var pendingPhotoBase64: String? = null
    private var photoRemoved: Boolean = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) handlePickedImage(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentProfile()
        setupListeners()
    }

    private fun loadCurrentProfile() {
        val user = authRepository.getCurrentUser() ?: return

        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                binding.etUserName.setText(name)

                val storedPhoto = doc.getString("profileImageBase64")
                Base64Image.decode(storedPhoto)?.let { bmp ->
                    binding.ivAvatar.setImageBitmap(bmp)
                }
            }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.tvUploadRemove.setOnClickListener { showPhotoOptions() }
        binding.ivAvatar.setOnClickListener { launchPicker() }
    }

    private fun showPhotoOptions() {
        val options = arrayOf(
            getString(R.string.edit_profile_photo_choose),
            getString(R.string.edit_profile_photo_remove)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_profile_photo_action_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchPicker()
                    1 -> markPhotoRemoved()
                }
            }
            .show()
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
        pendingPhotoBase64 = encoded
        photoRemoved = false
        Base64Image.decode(encoded)?.let { binding.ivAvatar.setImageBitmap(it) }
    }

    private fun markPhotoRemoved() {
        pendingPhotoBase64 = null
        photoRemoved = true
        binding.ivAvatar.setImageResource(R.drawable.ic_generic_avatar_gray)
    }

    private fun saveProfile() {
        val newName = binding.etUserName.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (newName.isEmpty()) {
            binding.tilUserName.error = getString(R.string.validation_name_empty)
            return
        }

        if (newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (newPassword != confirmPassword) {
                binding.tilConfirmPassword.error = getString(R.string.validation_passwords_not_match)
                return
            }
            if (newPassword.length < 6) {
                binding.tilNewPassword.error = getString(R.string.validation_password_min_length)
                return
            }
            binding.tilConfirmPassword.error = null
            binding.tilNewPassword.error = null

            // Password update logic goes here via AuthRepository
        }

        val user = authRepository.getCurrentUser() ?: return

        val updates = mutableMapOf<String, Any>("name" to newName)
        when {
            pendingPhotoBase64 != null -> updates["profileImageBase64"] = pendingPhotoBase64!!
            photoRemoved -> updates["profileImageBase64"] = ""
        }

        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                UserAvatarCache.invalidate(user.uid)
                Toast.makeText(this, R.string.edit_profile_update_success, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.edit_profile_update_error, Toast.LENGTH_SHORT).show()
            }
    }
}
