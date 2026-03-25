package com.example.divideai.ui.groups.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.Group
import com.example.divideai.data.model.User
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.GroupRepository
import com.example.divideai.data.repository.MemberRepository
import com.example.divideai.data.repository.UserRepository

class GroupFormViewModel : ViewModel() {

    private val groupRepository = GroupRepository()
    private val memberRepository = MemberRepository()
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private var currentGroupId: String? = null

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


    fun saveGroup(title: String, description: String) {
        if (title.isBlank()) {
            _saveStatus.value = Pair(false, "O título não pode estar vazio")
            return
        }

        if (currentGroupId != null) {
            // Modo de edicao
            val group = Group(id = currentGroupId!!, title = title, description = description, memberIds =  _groupToEdit.value?.memberIds ?: emptyList() )
            groupRepository.updateGroup(group) { success, errorMessage ->
                _saveStatus.value = Pair(success, errorMessage)
            }
        } else {
            // Modo de criacao
            val currentUser = authRepository.getCurrentUser()
            val group = Group(title = title, description = description)

            groupRepository.addGroup(group) { success, errorMessage, newGroupId ->
                if (!success || newGroupId == null) {
                    _saveStatus.value = Pair(false, errorMessage ?: "Erro ao criar o grupo")
                    return@addGroup
                }

                if (currentUser == null) {
                    _saveStatus.value =
                        Pair(false, "Grupo criado, mas não há usuário autenticado no app.")
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
                                Pair(false, "Grupo criado, mas falha ao vincular seu usuário.")
                        }
                    }

                }


            }
        }
    }
}