package com.example.divideai.data.balance

import com.example.divideai.data.model.Expense
import kotlin.math.abs
import kotlin.math.min

/**
 * Pagamento mínimo necessário para zerar a dívida entre dois usuários.
 *
 * Significado: [debtorId] precisa transferir [amount] para [creditorId].
 */
data class SettlementTransfer(
    val debtorId: String,
    val creditorId: String,
    val amount: Double
)

/**
 * Aplica um algoritmo guloso de simplificação de dívidas:
 *
 *   1. Calcula o saldo líquido de cada usuário considerando todas as despesas
 *      passadas como entrada (pagamento − dívida).
 *   2. Separa em credores (saldo positivo) e devedores (saldo negativo).
 *   3. A cada passo, pareia o maior credor com o maior devedor e liquida o
 *      mínimo entre os dois — repete até zerar todos os saldos.
 *
 * O resultado é o conjunto mínimo (ou próximo do mínimo) de transferências
 * necessárias para que todos fiquem quites. Exemplo: se A deve R$10 a B, e
 * B deve R$10 a C, o algoritmo produz uma única transferência A → C R$10,
 * eliminando o intermediário.
 *
 * Apenas o saldo ainda em aberto (campo `paid = false` das shares) é
 * considerado — pagamentos já marcados como recebidos não entram no cálculo.
 */
object DebtSimplifier {

    /** Limite para considerar um saldo como zero (evita ruído de ponto flutuante). */
    private const val EPSILON = 0.01

    fun simplify(expenses: List<Expense>): List<SettlementTransfer> {
        val netBalance = computeNetBalances(expenses)
        return pairCreditorsAndDebtors(netBalance)
    }

    /**
     * Mapa `userId -> saldo líquido`. Valor positivo: tem a receber.
     * Valor negativo: tem a pagar.
     */
    private fun computeNetBalances(expenses: List<Expense>): MutableMap<String, Double> {
        val balance = mutableMapOf<String, Double>()
        for (expense in expenses) {
            for (share in expense.participants) {
                if (share.paid) continue
                if (share.userId == expense.payerId) continue
                balance.merge(expense.payerId, share.amountOwed, Double::plus)
                balance.merge(share.userId, -share.amountOwed, Double::plus)
            }
        }
        return balance
    }

    private fun pairCreditorsAndDebtors(balance: MutableMap<String, Double>): List<SettlementTransfer> {
        val transfers = mutableListOf<SettlementTransfer>()

        // Trabalhamos em cima de listas mutáveis de pares para sempre escolher
        // o maior credor/devedor em O(log n) por iteração (sortedDescending é
        // recalculado a cada passo, suficiente para o tamanho típico de grupos).
        while (true) {
            val creditor = balance.maxByOrNull { it.value } ?: break
            val debtor = balance.minByOrNull { it.value } ?: break

            if (creditor.value <= EPSILON || debtor.value >= -EPSILON) break

            val amount = min(creditor.value, -debtor.value)
            transfers += SettlementTransfer(
                debtorId = debtor.key,
                creditorId = creditor.key,
                amount = amount
            )

            balance[creditor.key] = creditor.value - amount
            balance[debtor.key] = debtor.value + amount

            if (abs(balance[creditor.key] ?: 0.0) < EPSILON) balance.remove(creditor.key)
            if (abs(balance[debtor.key] ?: 0.0) < EPSILON) balance.remove(debtor.key)
        }

        return transfers
    }
}
