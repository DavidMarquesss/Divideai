package com.example.divideai.ui.balances

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.divideai.R
import com.example.divideai.data.balance.DebtSimplifier
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository
import com.example.divideai.data.repository.UserRepository

/**
 * Estado da tela [SimplifiedBalancesActivity].
 *  - youOwe       : valores que o usuário atual ainda deve a outras pessoas.
 *  - owedToYou    : valores que outras pessoas devem ao usuário atual.
 *  - isLoading    : exibe ProgressBar enquanto fetch + cálculo rodam.
 */
data class SimplifiedBalancesState(
    val youOwe: List<SimplifiedBalanceItem> = emptyList(),
    val owedToYou: List<SimplifiedBalanceItem> = emptyList(),
    val isLoading: Boolean = false
)

class SimplifiedBalancesViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val expenseRepository = ExpenseRepository()
    private val userRepository = UserRepository()

    private val _state = MutableLiveData(SimplifiedBalancesState(isLoading = true))
    val state: LiveData<SimplifiedBalancesState> = _state

    fun load() {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        _state.value = SimplifiedBalancesState(isLoading = true)

        expenseRepository.getAllInvolving(currentUserId) { expenses ->
            val transfers = DebtSimplifier.simplify(expenses)
            userRepository.getAllUsers { users ->
                val userMap = users.associateBy { it.id }
                val defaultName = getApplication<Application>().getString(R.string.default_user)
                fun nameOf(uid: String): String = userMap[uid]?.let {
                    it.name.ifEmpty { it.email.substringBefore('@') }
                } ?: defaultName

                val youOwe = transfers
                    .filter { it.debtorId == currentUserId }
                    .map { SimplifiedBalanceItem(it.creditorId, nameOf(it.creditorId), it.amount) }
                    .sortedByDescending { it.amount }

                val owedToYou = transfers
                    .filter { it.creditorId == currentUserId }
                    .map { SimplifiedBalanceItem(it.debtorId, nameOf(it.debtorId), it.amount) }
                    .sortedByDescending { it.amount }

                _state.value = SimplifiedBalancesState(
                    youOwe = youOwe,
                    owedToYou = owedToYou,
                    isLoading = false
                )
            }
        }
    }
}
