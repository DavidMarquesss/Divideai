package com.example.divideai.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.divideai.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }

        binding.registerButton.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextUser.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmationPassword.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                registerViewModel.register(name, email, password, confirmPassword)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isBlank()) {
            Toast.makeText(this, "Por favor, preencha o nome.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isBlank()) {
            Toast.makeText(this, "Por favor, preencha o e-mail.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isBlank()) {
            Toast.makeText(this, "Por favor, preencha a senha.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (confirmPassword.isBlank()) {
            Toast.makeText(this, "Por favor, confirme a senha.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {
        registerViewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterViewModel.RegisterState.Loading -> {
                    binding.registerButton.isEnabled = false
                    // You can show a progress bar here
                }
                is RegisterViewModel.RegisterState.Success -> {
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, "Registro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is RegisterViewModel.RegisterState.Error -> {
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
