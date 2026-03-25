package com.example.divideai.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.repository.AuthRepository

/**
 * ViewModel que gerencia operacoes gerais do perfil, atualmente focada
 * no fluxo de deslogamento (logout) do usuario do sistema.
 */
class ProfileViewModel: ViewModel() {

    private val authRepository = AuthRepository()

    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    /**
     * Encerra a sessao atual do usuario comunicando o repositorio de autenticacao
     * e emite um estado de sucesso para os observadores (Activity).
     */
    fun logout() {
        authRepository.logout()
        _logoutState.value = LogoutState.Success
    }

    /**
     * Estados possiveis durante o fluxo de logout.
     */
    sealed class LogoutState {
        object Success : LogoutState()
    }
}