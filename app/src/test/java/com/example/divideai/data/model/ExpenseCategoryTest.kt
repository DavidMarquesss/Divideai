package com.example.divideai.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Testes unitários (JVM pura, JUnit 4) da resolução de **categorias de
 * despesa**. Garante que o `id` gravado no Firestore sempre volta para a
 * categoria correta — e que um valor desconhecido/ausente cai num padrão
 * seguro em vez de quebrar a tela.
 */
class ExpenseCategoryTest {

    @Test
    fun `fromId resolve um id conhecido para a categoria certa`() {
        assertSame(ExpenseCategory.FOOD, ExpenseCategory.fromId("food"))
        assertSame(ExpenseCategory.TRANSPORT, ExpenseCategory.fromId("transport"))
    }

    @Test
    fun `fromId com id desconhecido cai na categoria padrao`() {
        assertSame(ExpenseCategory.DEFAULT, ExpenseCategory.fromId("pizza-de-madrugada"))
    }

    @Test
    fun `fromId com null ou vazio cai na categoria padrao`() {
        assertSame(ExpenseCategory.DEFAULT, ExpenseCategory.fromId(null))
        assertSame(ExpenseCategory.DEFAULT, ExpenseCategory.fromId(""))
    }

    @Test
    fun `a categoria padrao e OTHER`() {
        assertSame(ExpenseCategory.OTHER, ExpenseCategory.DEFAULT)
    }

    @Test
    fun `todos os ids persistidos sao unicos`() {
        // Ids repetidos fariam fromId() devolver a categoria errada.
        val ids = ExpenseCategory.entries.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `o id de cada categoria resolve de volta para ela mesma (round-trip)`() {
        for (category in ExpenseCategory.entries) {
            assertSame(category, ExpenseCategory.fromId(category.id))
        }
    }
}
