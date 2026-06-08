package com.example.divideai.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.divideai.MainActivity
import com.example.divideai.R
import com.example.divideai.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        loginViewModel.checkIfUserIsLoggedIn()
    }

    private fun setupClickListeners() {
        binding.tvCadastreSe.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginButton.setOnClickListener { attemptLogin() }

        // Pressing the "Done" key on the password field submits the form
        // instead of inserting a newline.
        binding.editTextPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.editTextUser.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        if (validateInput(email, password)) {
            loginViewModel.login(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank()) {
            Toast.makeText(this, R.string.validation_email_required, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.validation_email_invalid, Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isBlank()) {
            Toast.makeText(this, R.string.validation_password_required, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeViewModel() {
        loginViewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.loginButton.isEnabled = false
                    // You can show a progress bar here
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.loginButton.isEnabled = true
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.loginButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is LoginViewModel.LoginState.LoggedIn -> {
                    if (state.isLoggedIn) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}