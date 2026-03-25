package com.example.divideai.ui.receivables

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.divideai.data.model.ReceivableItem
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository

class UserReceivablesViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val expenseRepository = ExpenseRepository()

    private val _userExpenses = MutableLiveData<List<ReceivableItem>>()
    val userExpenses: LiveData<List<ReceivableItem>> = _userExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadUserExpenses(debtorId: String, debtorName: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        _isLoading.value = true

        expenseRepository.getReceivables(currentUserId) { expenses ->
            if (expenses.isEmpty()) {
                _userExpenses.value = emptyList()
                _isLoading.value = false
                return@getReceivables
            }

            val items = mutableListOf<ReceivableItem>()

            for (expense in expenses) {
                val share = expense.participants.find { it.userId == debtorId }
                if (share != null && share.amountOwed > 0.01) {
                    items.add(
                        ReceivableItem(
                            expenseId = expense.id,
                            title = expense.title,
                            date = expense.date,
                            debtorId = share.userId,
                            debtorName = debtorName,
                            amountOwed = share.amountOwed,
                            isPaid = share.paid
                        )
                    )
                }
            }
            
            // Sort by unpaid first
            items.sortBy { it.isPaid }

            _userExpenses.value = items
            _isLoading.value = false
        }
    }
}
