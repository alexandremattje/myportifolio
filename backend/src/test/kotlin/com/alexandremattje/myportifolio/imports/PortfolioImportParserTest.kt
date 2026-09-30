package com.alexandremattje.myportifolio.imports

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class PortfolioImportParserTest {
    private val parser = PortfolioImportParser()

    @Test
    fun `parses Brazilian CSV values and maps buy operation`() {
        val csv = """Data operação,Categoria,Código Ativo,Operação C/V,Quantidade,Preço unitário,Corretora,Corretagem,Taxas,Impostos,IRRF
03/03/2021,Ação,WEGE3,C,1.234,1.234,56,Corretora X,2,50,0,0,0
"""
        // Quoted values are required when CSV fields contain commas.
        val validCsv = """Data operação,Categoria,Código Ativo,Operação C/V,Quantidade,Preço unitário,Corretora,Corretagem,Taxas,Impostos,IRRF
03/03/2021,Ação,WEGE3,C,"1.234","1.234,56",Corretora X,"2,50","0,00","0,00","0,00"
"""
        val preview = parser.parse("transactions.csv", ByteArrayInputStream(validCsv.toByteArray(StandardCharsets.UTF_8)))
        val transaction = preview.transactions.single()
        assertTrue(transaction.isValid)
        assertEquals(ImportedOperation.BUY, transaction.operation)
        assertEquals("1234", transaction.quantity.toString())
        assertEquals("1234.56", transaction.unitPrice.toString())
        assertEquals("2.50", transaction.brokerage.toString())
    }

    @Test
    fun `returns row validation errors rather than dropping malformed transactions`() {
        val csv = """Data operação,Código Ativo,Operação C/V,Quantidade,Preço unitário
31/02/2024,ABCD3,X,0,abc
"""
        val preview = parser.parse("transactions.csv", ByteArrayInputStream(csv.toByteArray(StandardCharsets.UTF_8)))
        val transaction = preview.transactions.single()
        assertFalse(transaction.isValid)
        assertTrue(transaction.errors.isNotEmpty())
        assertEquals(2, transaction.sourceRow)
    }
}
