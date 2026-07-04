package com.example.divideai.data.balance

import com.example.divideai.data.model.Expense
import com.example.divideai.data.model.ExpenseShare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários (JVM pura, JUnit 4) do algoritmo de simplificação de dívidas.
 *
 * [DebtSimplifier] é Kotlin puro — não depende de Android nem do Firebase —,
 * por isso roda no source set `test` com `./gradlew test`, sem emulador.
 *
 * Convenção do modelo: em cada [Expense], `payerId` é quem pagou e cada
 * [ExpenseShare] em `participants` indica quanto aquele `userId` deve ao pagador.
 */
class DebtSimplifierTest {

    /** Uma despesa em que [payer] adiantou [amount] por [debtor] (débito 1-para-1). */
    private fun debt(payer: String, debtor: String, amount: Double, paid: Boolean = false) =
        Expense(
            payerId = payer,
            participants = listOf(ExpenseShare(userId = debtor, amountOwed = amount, paid = paid))
        )

    @Test
    fun `cadeia A deve B e B deve C vira uma unica transferencia A para C`() {
        // B pagou por A (A deve 10 a B); C pagou por B (B deve 10 a C).
        val expenses = listOf(
            debt(payer = "B", debtor = "A", amount = 10.0),
            debt(payer = "C", debtor = "B", amount = 10.0)
        )

        val transfers = DebtSimplifier.simplify(expenses)

        // O intermediário B some: sobra apenas A -> C.
        assertEquals(1, transfers.size)
        assertEquals("A", transfers[0].debtorId)
        assertEquals("C", transfers[0].creditorId)
        assertEquals(10.0, transfers[0].amount, EPSILON)
    }

    @Test
    fun `divida direta gera uma transferencia do devedor para o credor`() {
        // A pagou 25 por B => B deve 25 a A.
        val transfers = DebtSimplifier.simplify(listOf(debt(payer = "A", debtor = "B", amount = 25.0)))

        assertEquals(1, transfers.size)
        assertEquals("B", transfers[0].debtorId)
        assertEquals("A", transfers[0].creditorId)
        assertEquals(25.0, transfers[0].amount, EPSILON)
    }

    @Test
    fun `parcelas ja pagas nao entram no calculo`() {
        // Único débito já está quitado (paid = true) => ninguém deve nada.
        val transfers = DebtSimplifier.simplify(
            listOf(debt(payer = "A", debtor = "B", amount = 10.0, paid = true))
        )

        assertTrue(transfers.isEmpty())
    }

    @Test
    fun `lista de despesas vazia nao gera transferencias`() {
        assertTrue(DebtSimplifier.simplify(emptyList()).isEmpty())
    }

    @Test
    fun `despesa rateada gera uma transferencia de cada devedor para o pagador`() {
        // A pagou um jantar de 30 dividido entre A, B e C (10 cada).
        // A parcela do próprio pagador é ignorada; B e C devem 10 cada a A.
        val jantar = Expense(
            payerId = "A",
            participants = listOf(
                ExpenseShare(userId = "A", amountOwed = 10.0), // parcela do pagador: ignorada
                ExpenseShare(userId = "B", amountOwed = 10.0),
                ExpenseShare(userId = "C", amountOwed = 10.0)
            )
        )

        val transfers = DebtSimplifier.simplify(listOf(jantar))

        // Duas transferências, ambas para o credor A, de 10 cada.
        assertEquals(2, transfers.size)
        assertTrue(transfers.all { it.creditorId == "A" })
        assertEquals(setOf("B", "C"), transfers.map { it.debtorId }.toSet())
        assertEquals(20.0, transfers.sumOf { it.amount }, EPSILON)
    }

    private companion object {
        /** Mesma tolerância de ponto flutuante usada pelo DebtSimplifier. */
        const val EPSILON = 0.01
    }
}
