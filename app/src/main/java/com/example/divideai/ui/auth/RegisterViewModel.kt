package com.example.divideai.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divideai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                repository.signUp(name, email, password)
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "A senha é muito fraca. Tente uma senha mais forte."
                    is FirebaseAuthInvalidCredentialsException -> "O e-mail fornecido é inválido."
                    is FirebaseAuthUserCollisionException -> "Este e-mail já está em uso."
                    else -> "Ocorreu um erro desconhecido. Tente novamente mais tarde."
                }
                _registerState.value = RegisterState.Error(errorMessage)
            }
        }
    }

    sealed class RegisterState {
        object Loading : RegisterState()
        object Success : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}