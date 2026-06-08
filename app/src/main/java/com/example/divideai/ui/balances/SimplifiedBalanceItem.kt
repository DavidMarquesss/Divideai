package com.example.divideai.ui.balances

/**
 * Saldo líquido entre o usuário atual e outro usuário.
 *
 * Se [amount] > 0, é dívida do usuário atual para [otherUserName].
 * Se [amount] < 0, é crédito que o atual tem a receber.
 * O sinal de fato é tratado nas listas distintas da [SimplifiedBalancesActivity];
 * aqui mantemos sempre o valor absoluto, mais simples para a renderização.
 */
data class SimplifiedBalanceItem(
    val otherUserId: String,
    val otherUserName: String,
    val amount: Double
)
