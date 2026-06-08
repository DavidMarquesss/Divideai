package com.example.divideai.ui.groups.form

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.divideai.R
import com.example.divideai.data.model.Group
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.GroupRepository
import com.example.divideai.data.repository.MemberRepository
import com.example.divideai.data.repository.UserRepository

class GroupFormViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository = GroupRepository()
    private val memberRepository = MemberRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private var currentGroupId: String? = null
    private var pendingImage: String? = null

    private val _saveStatus = MutableLiveData<Pair<Boolean, String?>>()
    val saveStatus: LiveData<Pair<Boolean, String?>> = _saveStatus

    // para definir se o form vai criar ou editar um grupo
    private val _groupToEdit = MutableLiveData<Group?>()
    val groupToEdit: LiveData<Group?> = _groupToEdit


    fun loadGroupForEdit(groupId: String) {
        currentGroupId = groupId
        groupRepository.getGroupById(groupId) { group ->
            _groupToEdit.value = group
        }
    }

    fun setImage(base64: String) {
        pendingImage = base64
    }

    private fun resolveImage(): String =
        pendingImage ?: _groupToEdit.value?.imageBase64 ?: ""

    fun saveGroup(title: String, description: String) {
        val app = getApplication<Application>()
        if (title.isBlank()) {
            _saveStatus.value = Pair(false, app.getString(R.string.error_group_title_empty))
            return
        }

        if (currentGroupId != null) {
            // Modo de edicao
            val group = Group(
                id = currentGroupId!!,
                title = title,
                description = description,
                memberIds = _groupToEdit.value?.memberIds ?: emptyList(),
                imageBase64 = resolveImage()
            )
            groupRepository.updateGroup(group) { success, errorMessage ->
                _saveStatus.value = Pair(success, errorMessage)
            }
        } else {
            // Modo de criacao
            val currentUser = authRepository.getCurrentUser()
            val group = Group(title = title, description = description, imageBase64 = resolveImage())

            groupRepository.addGroup(group) { success, errorMessage, newGroupId ->
                if (!success || newGroupId == null) {
                    _saveStatus.value = Pair(false, errorMessage ?: app.getString(R.string.error_creating_group))
                    return@addGroup
                }

                if (currentUser == null) {
                    _saveStatus.value =
                        Pair(false, app.getString(R.string.error_group_created_no_user))
                    return@addGroup
                }

                userRepository.getUserById(currentUser.uid) { fetchedUser ->
                    val userToAdd = fetchedUser ?: User(
                        id = currentUser.uid,
                        email = currentUser.email ?: "sem_email",
                        name = currentUser.displayName ?: "sem_nome"
                    )

                    memberRepository.addMembersToGroup(
                        newGroupId, listOf(userToAdd)
                    ) { memberSuccess ->
                        if (memberSuccess) {
                            _saveStatus.value = Pair(true, null)
                        } else {
                            _saveStatus.value =
                                Pair(false, app.getString(R.string.error_group_created_link_failed))
                        }
                    }

                }


            }
        }
    }
}