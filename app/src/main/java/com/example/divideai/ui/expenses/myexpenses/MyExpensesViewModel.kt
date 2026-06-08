package com.example.divideai.ui.expenses.myexpenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.divideai.R
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository
import com.example.divideai.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ExpenseFilter {
    ALL, PAID, UNPAID
}

data class MyExpensesState(
    val expenses: List<MyExpenseUiModel> = emptyList(),
    val currentFilter: ExpenseFilter = ExpenseFilter.ALL,
    val totalToPay: Double = 0.0,
    val isLoading: Boolean = false
)

class MyExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val expenseRepository = ExpenseRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(MyExpensesState(isLoading = true))
    val uiState: StateFlow<MyExpensesState> = _uiState.asStateFlow()

    private var allMockExpenses = mutableListOf<MyExpenseUiModel>()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        
        _uiState.update { it.copy(isLoading = true) }

        expenseRepository.getMyExpenses(currentUserId) { expenses ->
            if (expenses.isEmpty()) {
                allMockExpenses = mutableListOf()
                updateState()
                _uiState.update { it.copy(isLoading = false) }
                return@getMyExpenses
            }

            userRepository.getAllUsers { allUsers ->
                val userMap = allUsers.associateBy { it.id }
                val app = getApplication<Application>()
                val defaultUserLabel = app.getString(R.string.default_user)
                val youLabel = app.getString(R.string.label_you)

                val uiModels = expenses.map { expense ->
                    val payerNameReal = userMap[expense.payerId]?.name ?: defaultUserLabel
                    val isPaidByMe = (expense.payerId == currentUserId)

                    MyExpenseUiModel(
                        id = expense.id,
                        title = expense.title,
                        description = expense.description,
                        amount = expense.amount,
                        date = expense.date,
                        isPaidByMe = isPaidByMe,
                        payerName = if (isPaidByMe) youLabel else payerNameReal,
                        category = expense.category,
                        participants = expense.participants.map { p ->
                            ParticipantUiModel(
                                // Maintain "u1" for the logged-in user to avoid breaking the Adapter
                                id = if (p.userId == currentUserId) "u1" else p.userId,
                                name = if (p.userId == currentUserId) youLabel else (userMap[p.userId]?.name ?: defaultUserLabel),
                                amountOwed = p.amountOwed,
                                hasPaid = p.paid
                            )
                        }
                    )
                }

                allMockExpenses = uiModels.toMutableList()
                updateState()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilter(filter: ExpenseFilter) {
        _uiState.update { it.copy(currentFilter = filter) }
        updateState()
    }

    fun markAsPaid(expenseId: String) {
        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        val index = allMockExpenses.indexOfFirst { it.id == expenseId }
        
        if (index != -1) {
            // Buscamos o Expense real no Firebase para alterá-lo e salvar
            expenseRepository.getExpenseById(expenseId) { dbExpense ->
                if (dbExpense != null) {
                    val updatedParticipants = dbExpense.participants.map { 
                        if (it.userId == currentUserId) it.copy(paid = true) else it
                    }
                    val newDbExpense = dbExpense.copy(participants = updatedParticipants)
                    
                    expenseRepository.updateExpense(newDbExpense) { success, _ -> 
                        if (success) {
                            // Atualiza a representação da UI
                            val oldUiExpense = allMockExpenses[index]
                            val updatedUiParticipants = oldUiExpense.participants.map {
                                if (it.id == "u1") it.copy(hasPaid = true) else it
                            }
                            allMockExpenses[index] = oldUiExpense.copy(
                                isPaidByMe = (dbExpense.payerId == currentUserId),
                                participants = updatedUiParticipants
                            )
                            updateState()
                        }
                    }
                }
            }
        }
    }

    private fun updateState() {
        val currentFilter = _uiState.value.currentFilter
        
        val filteredList = when (currentFilter) {
            ExpenseFilter.ALL -> allMockExpenses.toList()
            ExpenseFilter.PAID -> allMockExpenses.filter { expense ->
                val hasPaidMyShare = expense.isPaidByMe || expense.participants.find { it.id == "u1" }?.hasPaid == true
                hasPaidMyShare
            }
            ExpenseFilter.UNPAID -> allMockExpenses.filter { expense ->
                val hasPaidMyShare = expense.isPaidByMe || expense.participants.find { it.id == "u1" }?.hasPaid == true
                !hasPaidMyShare
            }
        }

        // Calcula o total a pagar de todas as despesas onde "Você" (u1) ainda não pagou sua parte
        val total = allMockExpenses
            .flatMap { it.participants }
            .filter { it.id == "u1" && !it.hasPaid }
            .sumOf { it.amountOwed }

        _uiState.update { it.copy(
            expenses = filteredList,
            totalToPay = total
        ) }
    }
}