package com.example.divideai.ui.expenses

import com.example.divideai.ui.expenses.myexpenses.MyExpenseUiModel
import com.example.divideai.ui.expenses.myexpenses.ParticipantUiModel


object MockData {
    val expenses = mutableListOf(
        MyExpenseUiModel(
            id = "1", title = "Bolo de Aniversário", description = "Comprado na padaria Monza. Sabor chocolate.",
            amount = 100.0, date = "18/02/2026, 21h", isPaidByMe = false, payerName = "João",
            participants = listOf(
                ParticipantUiModel("u1", "Você", 50.0, false),
                ParticipantUiModel("u2", "João", 50.0, true)
            )
        ),
        MyExpenseUiModel(
            id = "2", title = "Netflix", description = "Assinatura mensal",
            amount = 55.90, date = "16/02/2026, 14h", isPaidByMe = true, payerName = "Você",
            participants = listOf(
                ParticipantUiModel("u1", "Você", 27.95, true),
                ParticipantUiModel("u3", "Maria", 27.95, true)
            )
        ),
        MyExpenseUiModel(
            id = "3", title = "Pizza sexta", description = "Rodízio na pizzaria da esquina",
            amount = 120.0, date = "14/02/2026, 20h", isPaidByMe = false, payerName = "Maria",
            participants = listOf(
                ParticipantUiModel("u1", "Você", 40.0, false),
                ParticipantUiModel("u2", "João", 40.0, true),
                ParticipantUiModel("u3", "Maria", 40.0, true)
            )
        ),
        MyExpenseUiModel(
            id = "4", title = "Presente Casamento", description = "Vakinha para os noivos",
            amount = 300.0, date = "09/02/2026, 12h", isPaidByMe = false, payerName = "Carlos",
            participants = listOf(
                ParticipantUiModel("u1", "Você", 150.0, true), // Você já pagou este
                ParticipantUiModel("u4", "Carlos", 150.0, true)
            )
        )
    )

    fun getExpenseById(id: String): MyExpenseUiModel? {
        return expenses.find { it.id == id }
    }
}
