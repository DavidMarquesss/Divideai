package com.example.divideai.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.UserRepository
import com.example.divideai.databinding.ActivityEditProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity responsavel pela edicao dos dados do perfil do usuario (como nome e senha).
 * Carrega os dados atuais usando o Firebase Firestore e permite salvar as modificacoes.
 */
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentProfile()
        setupListeners()
    }

    /**
     * Recupera os dados atuais do perfil do usuario logado a partir do Firestore.
     * Atualiza os campos de texto na tela (como o nome) com os dados recebidos.
     */
    private fun loadCurrentProfile() {
        val user = authRepository.getCurrentUser() ?: return
        
        FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                binding.etUserName.setText(name)
            }
    }

    /**
     * Configura os ouvintes de clique para os botoes da interface, incluindo
     * o botao para fechar a tela, salvar as alteracoes do perfil e remover/fazer upload de foto.
     */
    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.tvUploadRemove.setOnClickListener {
            Toast.makeText(this, "Upload de foto em breve", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Valida os novos dados inseridos pelo usuario e, se estiverem corretos,
     * atualiza o perfil no banco de dados Firestore. Trata tambem a logica
     * basica de validacao para mudanca de senha (ainda nao implementada na AuthRepository).
     */
    private fun saveProfile() {
        val newName = binding.etUserName.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (newName.isEmpty()) {
            binding.tilUserName.error = "Nome não pode ser vazio"
            return
        }

        if (newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (newPassword != confirmPassword) {
                binding.tilConfirmPassword.error = "As senhas não coincidem"
                return
            }
            if (newPassword.length < 6) {
                binding.tilNewPassword.error = "A senha deve ter pelo menos 6 caracteres"
                return
            }
            binding.tilConfirmPassword.error = null
            binding.tilNewPassword.error = null
            
            // Password update logic goes here via AuthRepository
        }

        val user = authRepository.getCurrentUser() ?: return
        
        // Update name in Firestore
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .update("name", newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
            }
    }
}
