package com.example.divideai.ui.groups


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.Group
import com.example.divideai.data.repository.GroupRepository
import com.example.divideai.data.repository.AuthRepository

class GroupsViewModel : ViewModel() {
    private val repository = GroupRepository()
    private val authRepository = AuthRepository()

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedIds = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedIds: LiveData<MutableSet<String>> = _selectedIds

    private var allGroupsBackup = listOf<Group>()

    private val _groupList = MutableLiveData<List<Group>>()
    val groupList: LiveData<List<Group>> = _groupList

    private val _deleteStatus = MutableLiveData<Pair<Boolean, String>?>()
    val deleteStatus: LiveData<Pair<Boolean, String>?> = _deleteStatus

    fun fetchGroups() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            repository.getGroups(currentUser.uid) { groups ->
                allGroupsBackup = groups
                _groupList.value = groups
            }
        }
    }

    fun filterGroups(query: String) {
        if (allGroupsBackup.isEmpty()) {
            fetchGroups()
        }
        if (query.isBlank()) {
            _groupList.value = allGroupsBackup
        } else {
            val filteredList = allGroupsBackup.filter { group ->
                group.title.contains(query, ignoreCase = true)
            }
            _groupList.value = filteredList
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value?.clear()
            _selectedIds.notifyObserver()
        }
    }

    fun toggleGroupSelection(groupId: String) {
        val currentSelection = _selectedIds.value ?: mutableSetOf()
        if (currentSelection.contains(groupId)) {
            currentSelection.remove(groupId)
        } else {
            currentSelection.add(groupId)
        }
        _selectedIds.value = currentSelection

        if (currentSelection.isEmpty()) {
            setSelectionMode(false)
        }
    }

    fun toggleSelectAll(isChecked: Boolean) {
        val currentSelection = _selectedIds.value ?: mutableSetOf()
        if (isChecked) {
            _groupList.value?.forEach { currentSelection.add(it.id) }
        } else {
            currentSelection.clear()
            setSelectionMode(false)
        }
        _selectedIds.value = currentSelection
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun deleteSelectedGroups() {
        val idsToDelete = _selectedIds.value?.toList() ?: return
        if (idsToDelete.isEmpty()) return

        repository.deleteGroups(idsToDelete) { success ->
            if (success) {
                setSelectionMode(false)
                fetchGroups()
                _deleteStatus.value = Pair(true, "Grupos excluídos com sucesso!")
            } else {
                _deleteStatus.value = Pair(false, "Erro ao excluir os grupos. Verifique sua conexão.")
            }
        }
    }

    fun clearDeleteStatus() {
        _deleteStatus.value = null
    }
}
