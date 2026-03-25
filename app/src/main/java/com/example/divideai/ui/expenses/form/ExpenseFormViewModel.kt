package com.example.divideai.ui.expenses.form

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.Expense
import com.example.divideai.data.model.ExpenseShare
import com.example.divideai.data.model.Member
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository
import com.example.divideai.data.repository.MemberRepository

class ExpenseFormViewModel : ViewModel() {

    private val repository = ExpenseRepository()
    private val memberRepository = MemberRepository()
    private val authRepository = AuthRepository()

    private val _saveStatus = MutableLiveData<Pair<Boolean, String?>>()
    val saveStatus: LiveData<Pair<Boolean, String?>> = _saveStatus


    // Lista de membros do grupo
    private val _allMembers = MutableLiveData<List<Member>>()
    val allMembers: LiveData<List<Member>> = _allMembers
    private val _availableMembers = MutableLiveData<List<Member>>()
    val availableMembers: LiveData<List<Member>> = _availableMembers

    // Quem é o pagador selecionado
    private val _selectedPayer = MutableLiveData<Member?>()
    val selectedPayer: LiveData<Member?> = _selectedPayer

    // Quais membros estão selecionados para dividir a conta
    private val _selectedParticipantIds = MutableLiveData<Set<String>>(emptySet())
    val selectedParticipantIds: LiveData<Set<String>> = _selectedParticipantIds


    fun loadGroupData(groupId: String) {
        memberRepository.getMembersByGroup(groupId) { memberList ->
            _allMembers.value = memberList

            val currentUserUid = authRepository.getCurrentUser()?.uid
            val defaultPayer =
                memberList.find { it.userId == currentUserUid } ?: memberList.firstOrNull()

            if (defaultPayer != null) {
                setPayer(defaultPayer)
            }

            _availableMembers.value = memberList.filter {
                it.id != _selectedPayer.value?.id
            }

            // Por padrão, todos os membros do grupo participam da divisão
            _selectedParticipantIds.value = _availableMembers.value?.map { it.id }?.toSet()


        }
    }

    fun setPayer(member: Member) {
        _selectedPayer.value = member
        _availableMembers.value = _allMembers.value.filter {
            it.id != _selectedPayer.value?.id
        }
        _selectedParticipantIds.value =
            _availableMembers.value?.map { it.id }?.toSet() ?: emptySet()
    }

    fun toggleParticipant(memberId: String) {
        val current = _selectedParticipantIds.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(memberId)) current.remove(memberId) else current.add(memberId)
        _selectedParticipantIds.value = current
    }

    fun saveExpense(groupId: String, title: String, description: String, amountString: String) {
        if (title.isBlank() || amountString.isBlank()) {
            _saveStatus.value = Pair(false, "Título e valor são obrigatórios.")
            return
        }

        val amount = amountString.replace(",", ".").toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            _saveStatus.value = Pair(false, "Insira um valor válido.")
            return
        }

        val payer = _selectedPayer.value
        if (payer == null) {
            _saveStatus.value = Pair(false, "Selecione quem pagou a despesa.")
            return
        }

        val participants = _selectedParticipantIds.value?.toList() ?: emptyList()
        if (participants.isEmpty()) {
            _saveStatus.value = Pair(false, "Selecione pelo menos um participante.")
            return
        }

        val totalPeopleToSplit = participants.size + 1
        val allGroupMembers = _allMembers.value ?: emptyList()
        // Valor que cada um deve pagar
        val amountPerPerson = amount / totalPeopleToSplit
        val shares = participants.mapNotNull { memberId ->
            val memberObj = allGroupMembers.find { it.id == memberId }

            if (memberObj != null) {
                ExpenseShare(
                    userId = memberObj.userId,
                    amountOwed = amountPerPerson,
                    paid = false
                )
            } else {
                null
            }
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val expense = Expense(
            groupId = groupId,
            title = title,
            description = description,
            amount = amount,
            date = currentDate,
            payerId = payer.userId,
            participants = shares
        )

        repository.addExpense(expense) { success, errorMessage ->
            _saveStatus.value = Pair(success, errorMessage)
        }
    }
}