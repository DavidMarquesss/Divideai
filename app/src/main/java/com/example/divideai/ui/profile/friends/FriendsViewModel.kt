package com.example.divideai.ui.profile.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divideai.data.model.FriendRequest
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.FriendRepository
import kotlinx.coroutines.launch

/**
 * ViewModel utilizado para gerenciar e manter os dados relacionados a lista de amigos
 * e solicitacoes de amizade. Ele coordena a busca desses dados no [FriendRepository].
 */
class FriendsViewModel : ViewModel() {
    private val friendRepository = FriendRepository()
    private val authRepository = AuthRepository()

    private val _friends = MutableLiveData<List<FriendRequest>>()
    val friends: LiveData<List<FriendRequest>> = _friends

    private val _pendingRequests = MutableLiveData<List<FriendRequest>>()
    val pendingRequests: LiveData<List<FriendRequest>> = _pendingRequests

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    /**
     * Carrega tanto a lista de amigos ativos quanto a lista de solicitacoes de amizade pendentes.
     * Atualiza os LiveDatas correspondentes para refletir o estado de carregamento e os dados.
     */
    fun loadData() {
        val currentUser = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            
            // Note: in a real app these could be run concurrently with async/await
            _friends.value = friendRepository.getFriends(currentUser.uid)
            _pendingRequests.value = friendRepository.getPendingRequests(currentUser.uid)
            
            _isLoading.value = false
        }
    }

    /**
     * Permite que o usuario responda a uma solicitacao de amizade recebida.
     * Caso a resposta seja processada com sucesso, os dados (listas) sao recarregados automaticamente.
     *
     * @param requestId O ID do documento da solicitacao de amizade.
     * @param accept Verdadeiro (true) se a solicitacao foi aceita, falso (false) se foi recusada.
     */
    fun respondToRequest(requestId: String, accept: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = friendRepository.respondToRequest(requestId, accept)
            if (result.isSuccess) {
                // reload data to reflect changes
                loadData()
            }
            _isLoading.value = false
        }
    }
}