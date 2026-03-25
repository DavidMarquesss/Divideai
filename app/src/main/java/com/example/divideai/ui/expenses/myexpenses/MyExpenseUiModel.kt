package com.example.divideai.ui.expenses.myexpenses

data class MyExpenseUiModel(
    val id: String,
    val title: String,
    val description: String,
    val amount: Double,
    val date: String,
    val isPaidByMe: Boolean, // Status em relação ao usuário logado
    val payerName: String,   // Quem pagou a conta inteira
    val participants: List<ParticipantUiModel>
)

data class ParticipantUiModel(
    val id: String,
    val name: String,
    val amountOwed: Double,
    val hasPaid: Boolean
)
