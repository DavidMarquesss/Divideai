package com.example.divideai.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divideai.R
import com.example.divideai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

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
                    is FirebaseAuthWeakPasswordException -> getApplication<Application>().getString(R.string.error_auth_weak_password)
                    is FirebaseAuthInvalidCredentialsException -> getApplication<Application>().getString(R.string.error_auth_invalid_email_format)
                    is FirebaseAuthUserCollisionException -> getApplication<Application>().getString(R.string.error_auth_email_in_use)
                    else -> getApplication<Application>().getString(R.string.error_auth_register_generic)
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
