package com.example.divideai.ui.dashboard

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.divideai.data.model.Expense
import com.example.divideai.data.model.ExpenseCategory
import com.example.divideai.data.repository.AuthRepository
import com.example.divideai.data.repository.ExpenseRepository

data class DashboardState(
    val total: Double = 0.0,
    val breakdown: List<CategoryBreakdownItem> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val expenseRepository = ExpenseRepository()

    private val _state = MutableLiveData(DashboardState(isLoading = true))
    val state: LiveData<DashboardState> = _state

    fun load() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        _state.value = DashboardState(isLoading = true)
        expenseRepository.getAllInvolving(userId) { expenses ->
            _state.value = computeState(userId, expenses)
        }
    }

    /**
     * A "fatia" do usuário em cada despesa é distribuída uniformemente: o
     * pagador também consome uma cota (a despesa cobre `participants + 1`
     * pessoas). Se o usuário aparece nos participantes, basta usar a
     * `amountOwed` registrada.
     */
    private fun computeState(userId: String, expenses: List<Expense>): DashboardState {
        val totalsByCategory = mutableMapOf<ExpenseCategory, Double>()
        var grandTotal = 0.0

        for (expense in expenses) {
            val myShare = userShareOf(expense, userId)
            if (myShare <= 0.0) continue
            val category = ExpenseCategory.fromId(expense.category)
            totalsByCategory.merge(category, myShare, Double::plus)
            grandTotal += myShare
        }

        val breakdown = totalsByCategory
            .toList()
            .sortedByDescending { it.second }
            .mapIndexed { index, (cat, total) ->
                CategoryBreakdownItem(cat, total, PALETTE[index % PALETTE.size])
            }

        return DashboardState(total = grandTotal, breakdown = breakdown, isLoading = false)
    }

    private fun userShareOf(expense: Expense, userId: String): Double {
        if (expense.payerId == userId) {
            val splitCount = expense.participants.size + 1
            if (splitCount <= 0) return 0.0
            return expense.amount / splitCount
        }
        return expense.participants.firstOrNull { it.userId == userId }?.amountOwed ?: 0.0
    }

    companion object {
        /** Paleta categórica suave e amigável a dark mode. */
        private val PALETTE = intArrayOf(
            Color.parseColor("#8FBB99"), // muted teal
            Color.parseColor("#F4A261"), // soft orange
            Color.parseColor("#E76F51"), // coral
            Color.parseColor("#2A9D8F"), // teal
            Color.parseColor("#E9C46A"), // mustard
            Color.parseColor("#5E60CE"), // indigo
            Color.parseColor("#B5179E"), // magenta
            Color.parseColor("#06A77D"), // emerald
            Color.parseColor("#7D7C84")  // neutral
        )
    }
}
