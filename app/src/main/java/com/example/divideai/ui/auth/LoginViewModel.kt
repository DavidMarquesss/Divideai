package com.example.divideai.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divideai.R
import com.example.divideai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

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
                    is FirebaseAuthInvalidUserException -> getApplication<Application>().getString(R.string.error_auth_user_not_found)
                    is FirebaseAuthInvalidCredentialsException -> getApplication<Application>().getString(R.string.error_auth_invalid_credentials)
                    else -> getApplication<Application>().getString(R.string.error_auth_login_generic)
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
