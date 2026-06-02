package com.example.divideai.ui.groups.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.divideai.R
import com.example.divideai.data.model.Expense
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository

class GroupExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseRepository()
    private val authRepository = AuthRepository()

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedIds = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val selectedIds: LiveData<MutableSet<String>> = _selectedIds

    private var allExpensesBackup = listOf<Expense>()

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseList: LiveData<List<Expense>> = _expenseList

    private val _deleteStatus = MutableLiveData<Pair<Boolean, String>?>()
    val deleteStatus: LiveData<Pair<Boolean, String>?> = _deleteStatus

    fun fetchExpenses(groupId: String) {
        repository.getExpensesByGroup(groupId) { list ->
            allExpensesBackup = list
            _expenseList.value = list
        }
    }

    fun filterExpenses(query: String) {
        if (query.isBlank()) {
            _expenseList.value = allExpensesBackup
        } else {
            val filtered = allExpensesBackup.filter {
                it.title.contains(query, ignoreCase = true)
            }
            _expenseList.value = filtered
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value?.clear()
            _selectedIds.notifyObserver() // Atualizado para usar o notifyObserver
        }
    }

    fun toggleSelection(expenseId: String) {
        val current = _selectedIds.value ?: mutableSetOf()
        if (current.contains(expenseId)) current.remove(expenseId) else current.add(expenseId)
        _selectedIds.value = current

        if (current.isEmpty()) setSelectionMode(false)
    }

    fun toggleSelectAll(isChecked: Boolean) {
        val current = _selectedIds.value ?: mutableSetOf()
        if (isChecked) {
            _expenseList.value?.forEach { current.add(it.id) }
        } else {
            current.clear()
            setSelectionMode(false)
        }
        _selectedIds.value = current
    }

    fun deleteSelectedExpenses(groupId: String) {
        val ids = _selectedIds.value?.toList() ?: return
        if (ids.isEmpty()) return

        repository.deleteExpenses(ids) { success ->
            val app = getApplication<Application>()
            if (success) {
                setSelectionMode(false)
                fetchExpenses(groupId)
                _deleteStatus.value = Pair(true, app.getString(R.string.success_expenses_deleted))
            } else {
                _deleteStatus.value = Pair(false, app.getString(R.string.error_deleting_expenses_connection))
            }
        }
    }

    fun getCurrentUserId(): String {
        return authRepository.getCurrentUser()?.uid ?: ""
    }

    fun clearDeleteStatus() { _deleteStatus.value = null }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun markAsPaid(expense: Expense, currentUserId: String) {
        val updatedParticipants = expense.participants.map { share ->
            if (share.userId == currentUserId) {
                share.copy(paid = true)
            } else {
                share
            }
        }

        val updatedExpense = expense.copy(participants = updatedParticipants)

        repository.updateExpense(updatedExpense) { success, _ ->
            if (success) {
                fetchExpenses(expense.groupId)
            }
        }
    }
}