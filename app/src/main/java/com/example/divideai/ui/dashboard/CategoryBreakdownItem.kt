package com.example.divideai.ui.dashboard

import com.example.divideai.data.model.ExpenseCategory

/** Total gasto pelo usuário atual em [category], usado pelo gráfico e pela lista. */
data class CategoryBreakdownItem(
    val category: ExpenseCategory,
    val total: Double,
    val colorArgb: Int
)
