package com.example.divideai.ui.receivables

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.divideai.R
import com.example.divideai.data.model.ReceivableUserItem
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository
import com.example.divideai.data.repository.UserRepository

class ReceivablesViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val expenseRepository = ExpenseRepository()
    private val userRepository = UserRepository()

    private val _receivableUsers = MutableLiveData<List<ReceivableUserItem>>()
    val receivableUsers: LiveData<List<ReceivableUserItem>> = _receivableUsers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadReceivableUsers() {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        _isLoading.value = true

        expenseRepository.getReceivables(currentUserId) { expenses ->
            if (expenses.isEmpty()) {
                _receivableUsers.value = emptyList()
                _isLoading.value = false
                return@getReceivables
            }

            userRepository.getAllUsers { allUsers ->
                val userMap = allUsers.associateBy { it.id }
                val debtorIds = mutableSetOf<String>()

                for (expense in expenses) {
                    for (share in expense.participants) {
                        // Skip if it's the current user or if amount owed is 0
                        if (share.userId == currentUserId || share.amountOwed <= 0.01) continue
                        debtorIds.add(share.userId)
                    }
                }
                
                val defaultUserLabel = getApplication<Application>().getString(R.string.default_user)
                val items = debtorIds.map { debtorId ->
                    val debtor = userMap[debtorId]
                    val debtorName = debtor?.name?.ifEmpty { debtor.email.split("@")[0] } ?: defaultUserLabel
                    ReceivableUserItem(debtorId, debtorName)
                }.sortedBy { it.debtorName }

                _receivableUsers.value = items
                _isLoading.value = false
            }
        }
    }
}