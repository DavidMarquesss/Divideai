package com.example.divideai.ui.profile.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divideai.R
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.FriendRepository
import kotlinx.coroutines.launch

/**
 * ViewModel responsavel por gerenciar as buscas de usuarios no sistema.
 * Mantem o estado da lista de resultados, status de carregamento e o
 * resultado das operacoes de envio de solicitacao de amizade.
 */
class SearchUserViewModel(application: Application) : AndroidViewModel(application) {
    private val friendRepository = FriendRepository()
    private val authRepository = AuthRepository()

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _requestStatus = MutableLiveData<Pair<Boolean, String>?>()
    val requestStatus: LiveData<Pair<Boolean, String>?> = _requestStatus

    /**
     * Realiza uma consulta no [FriendRepository] procurando usuarios cujo nome ou email
     * contenham o texto fornecido. A busca só é iniciada se o texto tiver 3 ou mais caracteres.
     *
     * @param query O termo de busca inserido pelo usuario.
     */
    fun searchUsers(query: String) {
        val currentUser = authRepository.getCurrentUser() ?: return
        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val results = friendRepository.searchUsers(query, currentUser.uid)
            _searchResults.value = results
            _isLoading.value = false
        }
    }

    /**
     * Envia uma solicitacao de amizade para o usuario especificado (destinatario).
     * O remetente sera o usuario atual logado. Atualiza [requestStatus] com
     * o resultado da operacao (sucesso ou falha).
     *
     * @param receiver O usuario destino que ira receber a solicitacao.
     */
    fun sendFriendRequest(receiver: User) {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            // Criamos um User temporário para o sender usando o email do Auth 
            // Já que não temos o Name completo do usuário logado carregado na memória aqui no momento
            // Ideal seria ter um UserRepository.getUserById
            val sender = User(id = currentUser.uid, email = currentUser.email ?: "")
            
            val result = friendRepository.sendFriendRequest(sender, receiver)
            val app = getApplication<Application>()
            if (result.isSuccess) {
                val targetName = receiver.name.ifEmpty { receiver.email }
                _requestStatus.value = Pair(true, app.getString(R.string.success_friend_request_sent_named, targetName))
            } else {
                _requestStatus.value = Pair(false, result.exceptionOrNull()?.message ?: app.getString(R.string.friend_request_generic_error))
            }
            _isLoading.value = false
        }
    }

    /**
     * Limpa a mensagem de status da ultima solicitacao (ex: apos um Toast
     * ja ter exibido o aviso na tela de busca).
     */
    fun clearStatus() {
        _requestStatus.value = null
    }
}