package com.example.divideai.data.invite

/**
 * Codifica e decodifica o conteúdo do QR Code usado para convites de grupo.
 *
 * Formato adotado: `divideai://group/<groupId>`. Mantemos um esquema custom
 * (em vez de uma URL https) para evitar que o scan abra um browser por engano.
 */
object GroupInviteCode {

    private const val PREFIX = "divideai://group/"

    fun encode(groupId: String): String = PREFIX + groupId

    /** Retorna `null` se o texto não for um convite válido do DivideAi. */
    fun decode(scanned: String?): String? {
        if (scanned.isNullOrBlank()) return null
        if (!scanned.startsWith(PREFIX)) return null
        return scanned.removePrefix(PREFIX).takeIf { it.isNotBlank() }
    }
}
