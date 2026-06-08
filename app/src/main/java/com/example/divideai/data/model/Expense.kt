package com.example.divideai.data.model

data class ExpenseShare(
    val userId: String = "",
    val amountOwed: Double = 0.0,
    val paid: Boolean = false
)

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val payerId: String = "",
    val participants: List<ExpenseShare> = emptyList(),
    val category: String = "",
    val receiptImageBase64: String = ""
)