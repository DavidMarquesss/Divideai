package com.example.divideai.data.balance

import com.example.divideai.data.model.Expense
import com.example.divideai.data.model.ExpenseShare
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testes unitários (JVM pura, JUnit 4) da regra de **divisão de despesa**
 * ([ExpenseSplit]). É a "conta de dividir" usada na criação da despesa e no
 * dashboard de gastos — por isso vale a pena ter travada por testes.
 *
 * Regra: o total é dividido igualmente entre o pagador e os participantes
 * (`participantes + 1` pessoas).
 */
class ExpenseSplitTest {

    @Test
    fun `divide o total entre o pagador e os participantes`() {
        // Jantar de 30 dividido entre o pagador + 2 participantes = 3 pessoas → 10 cada.
        assertEquals(10.0, ExpenseSplit.perPersonAmount(total = 30.0, participantCount = 2), EPSILON)
    }

    @Test
    fun `so o pagador (sem participantes) consome o total inteiro`() {
        assertEquals(50.0, ExpenseSplit.perPersonAmount(total = 50.0, participantCount = 0), EPSILON)
    }

    @Test
    fun `total nao-positivo resulta em zero (entrada invalida)`() {
        assertEquals(0.0, ExpenseSplit.perPersonAmount(total = 0.0, participantCount = 3), EPSILON)
        assertEquals(0.0, ExpenseSplit.perPersonAmount(total = -10.0, participantCount = 3), EPSILON)
    }

    @Test
    fun `a cota consumida pelo pagador e a fracao igualitaria`() {
        // A pagou 30 de um jantar dividido com B e C (participantes) → cota de A = 10.
        val jantar = Expense(
            payerId = "A",
            amount = 30.0,
            participants = listOf(ExpenseShare("B", 10.0), ExpenseShare("C", 10.0))
        )
        assertEquals(10.0, ExpenseSplit.shareConsumedBy(jantar, "A"), EPSILON)
    }

    @Test
    fun `a cota de um participante e o valor que ele deve`() {
        val jantar = Expense(
            payerId = "A",
            amount = 30.0,
            participants = listOf(ExpenseShare("B", 10.0), ExpenseShare("C", 10.0))
        )
        assertEquals(10.0, ExpenseSplit.shareConsumedBy(jantar, "B"), EPSILON)
    }

    @Test
    fun `quem nao participa da despesa consome zero`() {
        val jantar = Expense(
            payerId = "A",
            amount = 30.0,
            participants = listOf(ExpenseShare("B", 10.0), ExpenseShare("C", 10.0))
        )
        assertEquals(0.0, ExpenseSplit.shareConsumedBy(jantar, "Z"), EPSILON)
    }

    private companion object {
        const val EPSILON = 0.01
    }
}
