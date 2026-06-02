package com.example.divideai.data.model

data class ReceivableItem(
    val expenseId: String,
    val title: String,
    val date: String,
    val debtorId: String,
    val debtorName: String,
    val amountOwed: Double,
    val isPaid: Boolean,
    val category: String = ""
)
