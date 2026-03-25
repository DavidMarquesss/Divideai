package com.example.divideai.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divideai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun checkIfUserIsLoggedIn() {
        val isLoggedIn = repository.getCurrentUser() != null
        _loginState.value = LoginState.LoggedIn(isLoggedIn)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                repository.signIn(email, password)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Usuário não encontrado. Verifique o e-mail."
                    is FirebaseAuthInvalidCredentialsException -> "Credenciais inválidas. Verifique o e-mail e a senha."
                    else -> "Ocorreu um erro ao fazer o login. Tente novamente mais tarde."
                }
                _loginState.value = LoginState.Error(errorMessage)
            }
        }
    }

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
        data class LoggedIn(val isLoggedIn: Boolean) : LoginState()
    }
}