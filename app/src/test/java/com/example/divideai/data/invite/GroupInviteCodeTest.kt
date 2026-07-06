package com.example.divideai.data.invite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Testes unitários (JVM pura, JUnit 4) do [GroupInviteCode] — a codificação do
 * texto que vai dentro do **QR Code** de convite de grupo.
 *
 * É Kotlin puro (só manipula String), então roda com `./gradlew test`, sem
 * emulador.
 */
class GroupInviteCodeTest {

    @Test
    fun `encode gera o texto do convite com o esquema custom do app`() {
        // Usamos um esquema próprio (divideai://) para o scan NÃO abrir o navegador.
        assertEquals("divideai://group/abc123", GroupInviteCode.encode("abc123"))
    }

    @Test
    fun `decode de um convite valido devolve o id do grupo`() {
        assertEquals("abc123", GroupInviteCode.decode("divideai://group/abc123"))
    }

    @Test
    fun `encode e decode sao inversos (round-trip)`() {
        val groupId = "grupo-Da-Viagem-2025"
        assertEquals(groupId, GroupInviteCode.decode(GroupInviteCode.encode(groupId)))
    }

    @Test
    fun `decode ignora um texto que nao e convite do DivideAi`() {
        // Ex.: a pessoa escaneou um QR Code qualquer (um link https, um Wi-Fi...).
        assertNull(GroupInviteCode.decode("https://google.com"))
    }

    @Test
    fun `decode de nulo ou vazio devolve null`() {
        assertNull(GroupInviteCode.decode(null))
        assertNull(GroupInviteCode.decode(""))
        assertNull(GroupInviteCode.decode("   "))
    }

    @Test
    fun `decode de um convite sem id (so o prefixo) devolve null`() {
        // "divideai://group/" sem nada depois não identifica nenhum grupo.
        assertNull(GroupInviteCode.decode("divideai://group/"))
    }
}
