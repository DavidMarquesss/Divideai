package com.example.divideai.ui.groups.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.Member
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.MemberRepository
import com.example.divideai.data.repository.UserRepository
import com.example.divideai.data.repository.FriendRepository
import com.example.divideai.data.repository.AuthRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GroupMembersViewModel : ViewModel() {
    private val memberRepository = MemberRepository()
    private val userRepository = UserRepository()
    private val friendRepository = FriendRepository()
    private val authRepository = AuthRepository()

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedIds = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedIds: LiveData<MutableSet<String>> = _selectedIds

    private var allMembersBackup = listOf<Member>()

    private val _memberList = MutableLiveData<List<Member>>()
    val memberList: LiveData<List<Member>> = _memberList

    private val _deleteStatus = MutableLiveData<Pair<Boolean, String>?>()
    val deleteStatus: LiveData<Pair<Boolean, String>?> = _deleteStatus

    private val _availableUsers = MutableLiveData<List<User>?>()
    val availableUsers: LiveData<List<User>?> = _availableUsers

    private val _addStatus = MutableLiveData<Pair<Boolean, String>?>()
    val addStatus: LiveData<Pair<Boolean, String>?> = _addStatus

    fun fetchMembers(groupId: String) {
        memberRepository.getMembersByGroup(groupId) { list ->
            allMembersBackup = list
            _memberList.value = list
        }
    }

    fun filterMembers(query: String) {
        if (query.isBlank()) {
            _memberList.value = allMembersBackup
        } else {
            val filtered = allMembersBackup.filter {
                it.email.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true)
            }
            _memberList.value = filtered
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value?.clear()
            _selectedIds.notifyObserver()
        }
    }

    fun toggleSelection(memberId: String) {
        val current = _selectedIds.value ?: mutableSetOf()
        if (current.contains(memberId)) current.remove(memberId) else current.add(memberId)
        _selectedIds.value = current

        if (current.isEmpty()) setSelectionMode(false)
    }

    fun toggleSelectAll(isChecked: Boolean) {
        val current = _selectedIds.value ?: mutableSetOf()
        if (isChecked) {
            _memberList.value?.forEach { current.add(it.id) }
        } else {
            current.clear()
            setSelectionMode(false)
        }
        _selectedIds.value = current
    }

    fun deleteSelectedMembers(groupId: String) {
        val ids = _selectedIds.value?.toList() ?: return
        if (ids.isEmpty()) return

        memberRepository.deleteMembers(ids) { success ->
            if (success) {
                setSelectionMode(false)
                fetchMembers(groupId)
                _deleteStatus.value = Pair(true, "Membros removidos com sucesso!")
            } else {
                _deleteStatus.value = Pair(false, "Erro ao remover membros.")
            }
        }
    }

    fun fetchAvailableUsers(groupId: String) {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            try {
                // Fetch friends where I am sender OR receiver
                val friendsList = friendRepository.getFriends(currentUser.uid)
                val friendIds = friendsList.map { 
                    if (it.senderId == currentUser.uid) it.receiverId else it.senderId 
                }

                // If no friends, just return empty to the UI
                if (friendIds.isEmpty()) {
                    _availableUsers.value = emptyList()
                    return@launch
                }

                // Fetch the actual User objects for these friends
                userRepository.getAllUsers { allUsers ->
                    val myFriendUsers = allUsers.filter { user -> user.id in friendIds }

                    memberRepository.getMembersByGroup(groupId) { currentMembers ->
                        val currentMemberUserIds = currentMembers.map { it.userId }

                        // Filter out friends that are already in the group
                        val availableToInvite = myFriendUsers.filter { user ->
                            user.id !in currentMemberUserIds
                        }

                        _availableUsers.value = availableToInvite
                    }
                }
            } catch (e: Exception) {
                _availableUsers.value = emptyList()
            }
        }
    }

    fun clearAvailableUsers() {
        _availableUsers.value = null
    }

    fun addSelectedUsersToGroup(groupId: String, selectedUsers: List<User>) {
        if (selectedUsers.isEmpty()) return

        memberRepository.addMembersToGroup(groupId, selectedUsers) { success ->
            if (success) {
                _addStatus.value = Pair(true, "Membros adicionados com sucesso!")
                fetchMembers(groupId) // Recarrega a lista
            } else {
                _addStatus.value = Pair(false, "Erro ao adicionar membros.")
            }
        }
    }
    fun clearAddStatus() {
        _addStatus.value = null
    }


    fun clearDeleteStatus() { _deleteStatus.value = null }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }


}