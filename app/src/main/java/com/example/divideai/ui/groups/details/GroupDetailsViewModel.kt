package com.example.divideai.ui.groups.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.Group
import com.example.divideai.data.repository.GroupRepository

class GroupDetailsViewModel : ViewModel() {

    private val repository = GroupRepository()

    private val _group = MutableLiveData<Group?>()
    val group: LiveData<Group?> = _group

    private val _deleteStatus = MutableLiveData<Pair<Boolean, String?>?>()
    val deleteStatus: LiveData<Pair<Boolean, String?>?> = _deleteStatus

    fun loadGroup(groupId: String) {
        repository.getGroupById(groupId) { fetchedGroup ->
            _group.value = fetchedGroup
        }
    }


    fun deleteGroup(groupId: String) {
        repository.deleteGroup(groupId) { success, errorMessage ->
            _deleteStatus.value = Pair(success, errorMessage)
        }
    }

    fun clearDeleteStatus() {
        _deleteStatus.value = null
    }
}