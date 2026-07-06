package com.example.divideai.data.balance

import com.example.divideai.data.model.Expense

/**
 * Regras de **divisão de uma despesa** entre o pagador e os participantes.
 *
 * Convenção do app: uma despesa de [total] é dividida igualmente entre o
 * pagador e os `participantCount` participantes — ou seja, entre
 * `participantCount + 1` pessoas. O pagador também "consome" uma cota, mas não
 * fica devendo (ele já pagou); cada participante deve exatamente uma cota.
 *
 * A lógica vive aqui, em Kotlin puro, para poder ser testada isoladamente e
 * reaproveitada tanto na criação da despesa (formulário) quanto na leitura
 * (dashboard de gastos por categoria).
 */
object ExpenseSplit {

    /**
     * Valor que cada pessoa (pagador incluído) consome numa divisão
     * igualitária de [total] entre `participantCount + 1` pessoas.
     * Retorna `0.0` para entradas sem sentido (total não-positivo ou
     * contagem negativa), evitando divisões inválidas.
     */
    fun perPersonAmount(total: Double, participantCount: Int): Double {
        val people = participantCount + 1
        if (people <= 0 || total <= 0.0) return 0.0
        return total / people
    }

    /**
     * Quanto [userId] consumiu na [expense]. Se é o pagador, é a cota dele
     * (uma fração igualitária do total); caso contrário, é o valor já
     * registrado na participação dele — ou `0.0` se ele não participa.
     */
    fun shareConsumedBy(expense: Expense, userId: String): Double {
        if (expense.payerId == userId) {
            return perPersonAmount(expense.amount, expense.participants.size)
        }
        return expense.participants.firstOrNull { it.userId == userId }?.amountOwed ?: 0.0
    }
}
