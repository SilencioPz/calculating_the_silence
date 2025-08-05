package com.example.calculandoosilencio.data

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.layout.element.Cell
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class PdfExporter(private val context: Context) {

    fun exportMonthlyReport(
        transactions: List<Transaction>,
        month: Int,
        year: Int,
        balance: Double
    ): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Relatorio_${month}_${year}_${dateFormat.format(Date())}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val file = File(downloadsDir, fileName)

        PdfWriter(FileOutputStream(file)).use { writer ->
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Título
            document.add(
                Paragraph("Relatório Financeiro - ${getMonthName(month)}/$year")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18f)
            )

            // Saldo
            document.add(
                Paragraph("Saldo: R$ ${"%.2f".format(balance)}")
                    .setTextAlignment(TextAlignment.CENTER)
            )

            // Transações
            document.add(Paragraph("\nTransações:").setBold())
            transactions.forEach { transaction ->
                document.add(
                    Paragraph("${transaction.description} - R$ ${"%.2f".format(transaction.amount)} (${transaction.category})")
                )
            }

            document.close()
        }

        return file
    }

    fun exportAnnualReport(
        transactions: List<Transaction>,
        year: Int
    ): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Relatorio_Anual_Detalhado_${year}_${dateFormat.format(Date())}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val file = File(downloadsDir, fileName)

        PdfWriter(FileOutputStream(file)).use { writer ->
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // Título principal
            document.add(
                Paragraph("RELATÓRIO ANUAL DETALHADO - $year")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20f)
                    .setMarginBottom(20f)
            )

            // Resumo geral do ano
            val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }.absoluteValue
            val totalBalance = totalIncome - totalExpense

            document.add(
                Paragraph("RESUMO GERAL DO ANO")
                    .setBold()
                    .setFontSize(16f)
                    .setMarginTop(10f)
            )

            // Tabela resumo geral
            val summaryTable = Table(2)
            summaryTable.setWidth(UnitValue.createPercentValue(60f))

            summaryTable.addCell(createCell("Total de Entradas:", true))
            summaryTable.addCell(createCell("R$ ${"%.2f".format(totalIncome)}", false, ColorConstants.GREEN))

            summaryTable.addCell(createCell("Total de Saídas:", true))
            summaryTable.addCell(createCell("R$ ${"%.2f".format(totalExpense)}", false, ColorConstants.RED))

            summaryTable.addCell(createCell("Saldo Final:", true))
            summaryTable.addCell(createCell("R$ ${"%.2f".format(totalBalance)}", false,
                if (totalBalance >= 0) ColorConstants.GREEN else ColorConstants.RED))

            document.add(summaryTable)
            document.add(Paragraph("\n"))

            // Detalhamento por mês
            document.add(
                Paragraph("DETALHAMENTO POR MÊS")
                    .setBold()
                    .setFontSize(16f)
                    .setMarginTop(20f)
            )

            // Para cada mês do ano
            (1..12).forEach { month ->
                val monthTransactions = transactions.filter { it.month == month }

                if (monthTransactions.isNotEmpty()) {
                    // Título do mês
                    document.add(
                        Paragraph("\n${getMonthName(month).uppercase()} $year")
                            .setBold()
                            .setFontSize(14f)
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                            .setPadding(5f)
                    )

                    // Resumo do mês
                    val monthIncome = monthTransactions.filter { it.amount > 0 }.sumOf { it.amount }
                    val monthExpense = monthTransactions.filter { it.amount < 0 }.sumOf { it.amount }.absoluteValue
                    val monthBalance = monthIncome - monthExpense

                    val monthSummaryTable = Table(3)
                    monthSummaryTable.setWidth(UnitValue.createPercentValue(100f))

                    monthSummaryTable.addCell(createCell("Entradas: R$ ${"%.2f".format(monthIncome)}", false, ColorConstants.GREEN))
                    monthSummaryTable.addCell(createCell("Saídas: R$ ${"%.2f".format(monthExpense)}", false, ColorConstants.RED))
                    monthSummaryTable.addCell(createCell("Saldo: R$ ${"%.2f".format(monthBalance)}", false,
                        if (monthBalance >= 0) ColorConstants.GREEN else ColorConstants.RED))

                    document.add(monthSummaryTable)

                    // Tabela detalhada das transações do mês
                    val transactionTable = Table(4)
                    transactionTable.setWidth(UnitValue.createPercentValue(100f))
                    transactionTable.setMarginTop(10f)

                    // Cabeçalho da tabela
                    transactionTable.addHeaderCell(createHeaderCell("Descrição"))
                    transactionTable.addHeaderCell(createHeaderCell("Categoria"))
                    transactionTable.addHeaderCell(createHeaderCell("Valor"))
                    transactionTable.addHeaderCell(createHeaderCell("Tipo"))

                    // Separar entradas e saídas
                    val entries = monthTransactions.filter { it.amount > 0 }.sortedBy { it.description }
                    val expenses = monthTransactions.filter { it.amount < 0 }.sortedBy { it.description }

                    // Adicionar entradas primeiro
                    if (entries.isNotEmpty()) {
                        entries.forEach { transaction ->
                            transactionTable.addCell(createCell(transaction.description))
                            transactionTable.addCell(createCell(transaction.category))
                            transactionTable.addCell(createCell("R$ ${"%.2f".format(transaction.amount)}", false, ColorConstants.GREEN))
                            transactionTable.addCell(createCell("ENTRADA", false, ColorConstants.GREEN))
                        }
                    }

                    // Adicionar saídas
                    if (expenses.isNotEmpty()) {
                        expenses.forEach { transaction ->
                            transactionTable.addCell(createCell(transaction.description))
                            transactionTable.addCell(createCell(transaction.category))
                            transactionTable.addCell(createCell("R$ ${"%.2f".format(transaction.amount)}", false, ColorConstants.RED))
                            transactionTable.addCell(createCell("SAÍDA", false, ColorConstants.RED))
                        }
                    }

                    document.add(transactionTable)

                    // Análise por categoria do mês
                    val categoryExpenses = expenses.groupBy { it.category }
                        .mapValues { (_, transactions) -> transactions.sumOf { it.amount }.absoluteValue }
                        .toList()
                        .sortedByDescending { it.second }

                    if (categoryExpenses.isNotEmpty()) {
                        document.add(
                            Paragraph("\nGastos por Categoria em ${getMonthName(month)}:")
                                .setBold()
                                .setFontSize(12f)
                                .setMarginTop(10f)
                        )

                        val categoryTable = Table(2)
                        categoryTable.setWidth(UnitValue.createPercentValue(70f))

                        categoryTable.addHeaderCell(createHeaderCell("Categoria"))
                        categoryTable.addHeaderCell(createHeaderCell("Total Gasto"))

                        categoryExpenses.forEach { (category, amount) ->
                            categoryTable.addCell(createCell(category))
                            categoryTable.addCell(createCell("R$ ${"%.2f".format(amount)}", false, ColorConstants.RED))
                        }

                        document.add(categoryTable)
                    }

                    // Adicionar quebra de página entre meses (exceto o último)
                    if (month < 12) {
                        document.add(Paragraph("\n\n\n"))
                    }
                }
            }

            // Análise anual por categoria
            pdf.addNewPage()
            document.add(
                Paragraph("ANÁLISE ANUAL POR CATEGORIA")
                    .setBold()
                    .setFontSize(16f)
                    .setMarginTop(20f)
            )

            val annualCategoryExpenses = transactions
                .filter { it.amount < 0 }
                .groupBy { it.category }
                .mapValues { (_, transactions) -> transactions.sumOf { it.amount }.absoluteValue }
                .toList()
                .sortedByDescending { it.second }

            if (annualCategoryExpenses.isNotEmpty()) {
                val annualCategoryTable = Table(3)
                annualCategoryTable.setWidth(UnitValue.createPercentValue(80f))

                annualCategoryTable.addHeaderCell(createHeaderCell("Categoria"))
                annualCategoryTable.addHeaderCell(createHeaderCell("Total Anual"))
                annualCategoryTable.addHeaderCell(createHeaderCell("% do Total"))

                val totalAnnualExpenses = annualCategoryExpenses.sumOf { it.second }

                annualCategoryExpenses.forEach { (category, amount) ->
                    val percentage = (amount / totalAnnualExpenses) * 100
                    annualCategoryTable.addCell(createCell(category))
                    annualCategoryTable.addCell(createCell("R$ ${"%.2f".format(amount)}", false, ColorConstants.RED))
                    annualCategoryTable.addCell(createCell("${"%.1f".format(percentage)}%"))
                }

                document.add(annualCategoryTable)
            }

            // Rodapé
            document.add(
                Paragraph("\n\nRelatório gerado em: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f)
                    .setMarginTop(20f)
            )

            document.close()
        }

        return file
    }

    private fun createCell(content: String, bold: Boolean = false, color: com.itextpdf.kernel.colors.Color? = null): Cell {
        val cell = Cell().add(Paragraph(content))
        if (bold) cell.setBold()
        color?.let { cell.setFontColor(it) }
        cell.setPadding(5f)
        return cell
    }

    private fun createHeaderCell(content: String): Cell {
        return Cell().add(Paragraph(content))
            .setBold()
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setPadding(8f)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> "Mês $month"
        }
    }
}