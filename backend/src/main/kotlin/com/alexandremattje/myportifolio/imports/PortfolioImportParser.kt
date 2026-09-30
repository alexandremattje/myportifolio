package com.alexandremattje.myportifolio.imports

import org.apache.commons.csv.CSVFormat
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Component
import java.io.InputStream
import java.math.BigDecimal
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

enum class ImportedOperation { BUY, SELL }

data class ImportedTransaction(
    val sourceRow: Int,
    val tradeDate: LocalDate?,
    val category: String?,
    val symbol: String?,
    val operation: ImportedOperation?,
    val quantity: BigDecimal?,
    val unitPrice: BigDecimal?,
    val broker: String?,
    val brokerage: BigDecimal?,
    val fees: BigDecimal?,
    val taxes: BigDecimal?,
    val withholdingTax: BigDecimal?,
    val errors: List<String>,
    val warnings: List<String>
) {
    val isValid: Boolean get() = errors.isEmpty()
}

data class ImportPreview(
    val fileName: String,
    val transactions: List<ImportedTransaction>
) {
    val validCount: Int get() = transactions.count { it.isValid }
    val invalidCount: Int get() = transactions.size - validCount
}

@Component
class PortfolioImportParser {
    private val dateFormatter = DateTimeFormatter.ofPattern("d/M/uuuu")

    fun parse(fileName: String, input: InputStream): ImportPreview {
        val rows = when (fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
            "csv" -> parseCsv(input)
            "xlsx" -> parseXlsx(input)
            else -> throw IllegalArgumentException("Unsupported file type. Use .csv or .xlsx.")
        }
        require(rows.isNotEmpty()) { "The import file has no transaction rows." }
        val headers = rows.first().second
        val headerMap = headers.mapIndexed { index, value -> normalize(value) to index }.toMap()
        val dataRows = rows.drop(1)
        return ImportPreview(fileName, dataRows.map { (rowNumber, cells) ->
            parseRow(rowNumber, cells, headerMap)
        })
    }

    private fun parseCsv(input: InputStream): List<Pair<Int, List<String>>> {
        val reader = input.bufferedReader(Charsets.UTF_8)
        val records = CSVFormat.DEFAULT.builder()
            .setTrim(true)
            .build()
            .parse(reader)
            .records
        require(records.isNotEmpty()) { "CSV file is empty." }
        return records.mapIndexed { index, record -> (index + 1) to record.toList() }
    }

    private fun parseXlsx(input: InputStream): List<Pair<Int, List<String>>> {
        WorkbookFactory.create(input).use { workbook ->
            require(workbook.numberOfSheets > 0) { "Workbook has no worksheets." }
            val sheet = workbook.getSheet("Carteira") ?: workbook.getSheetAt(0)
            val formatter = DataFormatter(Locale("pt", "BR"))
            val rows = mutableListOf<Pair<Int, List<String>>>()
            for (rowIndex in sheet.firstRowNum..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val lastCell = row.lastCellNum.toInt().coerceAtLeast(0)
                val values = (0 until lastCell).map { col ->
                    formatter.formatCellValue(row.getCell(col)).trim()
                }
                if (values.any { it.isNotBlank() }) rows += (rowIndex + 1) to values
            }
            require(rows.isNotEmpty()) { "Worksheet is empty." }
            return rows
        }
    }

    private fun parseRow(
        rowNumber: Int,
        cells: List<String>,
        headerMap: Map<String, Int>
    ): ImportedTransaction {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        fun cell(vararg names: String): String? {
            val index = names.firstNotNullOfOrNull { headerMap[normalize(it)] } ?: return null
            return cells.getOrNull(index)?.trim()?.takeIf { it.isNotBlank() }
        }
        fun decimal(field: String, vararg names: String): BigDecimal? {
            val raw = cell(*names) ?: return null
            return try { parseBrazilianDecimal(raw) } catch (_: IllegalArgumentException) {
                errors += "Invalid $field value: '$raw'."
                null
            }
        }

        val date = cell("Data operação", "Data operacao", "Data", "Trade date")?.let {
            try { LocalDate.parse(it, dateFormatter) } catch (_: DateTimeParseException) {
                errors += "Invalid operation date '$it'; expected DD/MM/YYYY."
                null
            }
        } ?: run {
            if (errors.none { it.contains("operation date") }) errors += "Operation date is required."
            null
        }
        val symbol = cell("Código Ativo", "Codigo Ativo", "Ativo", "Ticker", "Symbol")
            ?: run { errors += "Asset symbol is required."; null }
        val operation = when (cell("Operação C/V", "Operacao C/V", "Operação", "Operacao", "Side")
            ?.uppercase(Locale.ROOT)) {
            "C", "COMPRA", "BUY" -> ImportedOperation.BUY
            "V", "VENDA", "SELL" -> ImportedOperation.SELL
            null -> { errors += "Operation is required."; null }
            else -> { errors += "Unknown operation; expected C/BUY or V/SELL."; null }
        }
        val quantity = decimal("quantity", "Quantidade", "Quantity")
        val unitPrice = decimal("unit price", "Preço unitário", "Preco unitario", "Preço", "Preco", "Unit price")
        if (quantity == null && errors.none { it.startsWith("Invalid quantity") }) errors += "Quantity is required."
        if (unitPrice == null && errors.none { it.startsWith("Invalid unit price") }) errors += "Unit price is required."
        if (quantity != null && quantity <= BigDecimal.ZERO) errors += "Quantity must be greater than zero."
        if (unitPrice != null && unitPrice < BigDecimal.ZERO) errors += "Unit price cannot be negative."

        val category = cell("Categoria", "Category")
        if (category.isNullOrBlank()) warnings += "Asset category is missing and needs confirmation."
        return ImportedTransaction(
            sourceRow = rowNumber,
            tradeDate = date,
            category = category,
            symbol = symbol?.uppercase(Locale.ROOT),
            operation = operation,
            quantity = quantity,
            unitPrice = unitPrice,
            broker = cell("Corretora", "Broker"),
            brokerage = decimal("brokerage", "Corretagem", "Brokerage") ?: BigDecimal.ZERO,
            fees = decimal("fees", "Taxas", "Fees") ?: BigDecimal.ZERO,
            taxes = decimal("taxes", "Impostos", "Taxes") ?: BigDecimal.ZERO,
            withholdingTax = decimal("withholding tax", "IRRF", "Withholding tax") ?: BigDecimal.ZERO,
            errors = errors,
            warnings = warnings
        )
    }

    private fun parseBrazilianDecimal(rawValue: String): BigDecimal {
        val raw = rawValue.trim().replace("R$", "", ignoreCase = true)
            .replace(" ", "")
        val normalized = when {
            raw.contains(',') -> raw.replace(".", "").replace(',', '.')
            raw.count { it == '.' } > 1 -> raw.replace(".", "")
            raw.matches(Regex("-?\\d{1,3}\\.\\d{3}")) -> raw.replace(".", "")
            else -> raw
        }
        return normalized.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Not a valid number")
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
}
