package com.example.calculandoosilencio.ui.theme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculandoosilencio.data.Transaction
import com.example.calculandoosilencio.ui.components.MonthYearSelector
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.calculandoosilencio.ui.components.AnnualChartScreen
import com.example.calculandoosilencio.ui.components.ExpenseChartScreen
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.calculandoosilencio.R
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import com.example.calculandoosilencio.data.PdfExporter
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.unit.sp

@Composable
fun TransactionScreen(viewModel: TransactionViewModel = viewModel()) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()

    val transactions by viewModel.transactions.collectAsState()
    val monthlyBalance by viewModel.monthlyBalance.collectAsState()

    val context = LocalContext.current

    var showChartScreen by remember { mutableStateOf(false) }

    var showAnnualChartScreen by remember { mutableStateOf(false) }
    var showAnnualAverageDialog by remember { mutableStateOf(false) }

    val annualAverage by viewModel.annualAverage.collectAsState()
    val annualExpenses by viewModel.annualExpensesByCategory.collectAsState()

    var isExportingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pdfExporter = remember { PdfExporter(context) }

    val allTransactionsSorted = remember(transactions) {
        transactions.sortedWith(
            compareByDescending<Transaction> { it.year }
                .thenByDescending { it.month }
                .thenByDescending { it.id }
        )
    }

    LaunchedEffect(showAnnualChartScreen) {
        println("DEBUG: ESTADO ATUAL - showAnnualChartScreen = $showAnnualChartScreen")
    }

    if (showAnnualChartScreen) {
        AnnualChartScreen(
            key = "annual_${currentYear}_${System.currentTimeMillis()}",
            transactions = transactions.filter { it.year == currentYear },
            selectedYear = currentYear,
            onBack = {
                showAnnualChartScreen = false
            },
            annualExpenses = annualExpenses
        )
        return
    }

    if (showAnnualAverageDialog) {
        AlertDialog(
            onDismissRequest = { showAnnualAverageDialog = false },
            title = { Text("Média Anual") },
            text = {
                Column {
                    Text("Média de gastos mensais em $currentYear:")
                    Text(
                        "R$ ${"%.2f".format(annualAverage)}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAnnualAverageDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showChartScreen) {
        ExpenseChartScreen(
            transactions = transactions.filter { it.month == currentMonth && it.year == currentYear },
            onBack = {
                println("Botão voltar pressionado")
                showChartScreen = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Calculando O Silencio",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = WhitePZ,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.silenciopz_logo),
                contentDescription = "Logo SilencioPZ",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(vertical = 8.dp),
                contentScale = ContentScale.Fit
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {

                MonthYearSelector(
                    selectedMonth = currentMonth,
                    selectedYear = currentYear,
                    onMonthChange = { viewModel.setPeriod(it, currentYear) },
                    onYearChange = { viewModel.setPeriod(currentMonth, it) }
                )

                Text(
                    "📊 Total de transações registradas: ${allTransactionsSorted.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (selectedTransaction != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "✏️ EDITANDO: ${selectedTransaction!!.description} (${selectedTransaction!!.month}/${selectedTransaction!!.year})",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição", color = WhitePZ) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = WhitePZ,
                        focusedTextColor = WhitePZ,
                        unfocusedLabelColor = WhitePZ,
                        focusedLabelColor = AccentPZ,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = AccentPZ,
                        cursorColor = AccentPZ
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Valor (use - para saída)", color = WhitePZ) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = WhitePZ,
                        focusedTextColor = WhitePZ,
                        unfocusedLabelColor = WhitePZ,
                        focusedLabelColor = AccentPZ,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = AccentPZ,
                        cursorColor = AccentPZ
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria", color = WhitePZ) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = WhitePZ,
                        focusedTextColor = WhitePZ,
                        unfocusedLabelColor = WhitePZ,
                        focusedLabelColor = AccentPZ,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = AccentPZ,
                        cursorColor = AccentPZ
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            if (selectedTransaction != null) {
                                viewModel.updateTransaction(
                                    selectedTransaction!!.copy(
                                        description = description,
                                        amount = amountValue,
                                        category = category
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "✅ Transação atualizada!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.addTransaction(
                                    Transaction(
                                        description = description,
                                        amount = amountValue,
                                        category = category,
                                        month = currentMonth,
                                        year = currentYear
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "✅ Transação adicionada!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            description = ""
                            amount = ""
                            category = ""
                            selectedTransaction = null
                        },
                        modifier = Modifier.weight(2f),
                        enabled = description.isNotEmpty() && amount.isNotEmpty() && category.isNotEmpty(),
                        colors = if (selectedTransaction != null) {
                            ButtonDefaults.buttonColors(
                                containerColor = AccentPZ,
                                contentColor = BlackPZ
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = AccentPZ,
                                contentColor = BlackPZ
                            )
                        }
                    ) {
                        Text(
                            if (selectedTransaction != null) "✏️ ATUALIZAR" else "➕ ADICIONAR"
                        )
                    }

                    Button(
                        onClick = {
                            description = ""
                            amount = ""
                            category = ""
                            selectedTransaction = null
                            Toast.makeText(context, "🧹 Campos limpos", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = description.isNotEmpty() || amount.isNotEmpty() || category.isNotEmpty() || selectedTransaction != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = WhitePZ
                        )
                    ) {
                        Text("🧹")
                    }

                    Button(
                        onClick = {
                            selectedTransaction?.let {
                                viewModel.deleteTransaction(it)
                                selectedTransaction = null
                                description = ""
                                amount = ""
                                category = ""
                                Toast.makeText(
                                    context,
                                    "🗑️ Transação excluída!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedTransaction != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = WhitePZ
                        )
                    ) {
                        Text("🗑️")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "📋 TODAS AS TRANSAÇÕES (${allTransactionsSorted.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (allTransactionsSorted.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "📝 Nenhuma transação encontrada!\n\nAdicione sua primeira transação acima.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val groupedTransactions = allTransactionsSorted.groupBy {
                            "${it.year}/${
                                String.format("%02d", it.month)
                            }"
                        }

                        groupedTransactions.forEach { (period, periodTransactions) ->
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                ) {
                                    val periodBalance = periodTransactions.sumOf { it.amount }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "📅 $period (${periodTransactions.size} transações)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = WhitePZ
                                        )
                                        Text(
                                            "💰 R$ ${"%.2f".format(periodBalance)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (periodBalance >= 0) Color(0xFF4CAF50) else Color(
                                                0xFFF44336
                                            )
                                        )
                                    }
                                }
                            }

                            items(periodTransactions) { transaction ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clickable {
                                            selectedTransaction = transaction
                                            description = transaction.description
                                            amount = transaction.amount.toString()
                                            category = transaction.category
                                            Toast.makeText(
                                                context,
                                                "✏️ Transação selecionada: ${transaction.description}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .let { modifier ->
                                            if (selectedTransaction?.id == transaction.id) {
                                                modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else modifier
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedTransaction?.id == transaction.id) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = transaction.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "🏷️ ${transaction.category}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                            Text(
                                                text = "R$ ${"%.2f".format(transaction.amount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (transaction.amount >= 0) Color(
                                                    0xFF4CAF50
                                                ) else Color(0xFFF44336)
                                            )
                                            Text(
                                                text = if (transaction.amount >= 0) "💰 Entrada" else "💸 Saída",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    if (selectedTransaction?.id == transaction.id) {
                                        Text(
                                            "👆 Transação selecionada - Use os botões acima para ATUALIZAR ou EXCLUIR",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 4.dp
                                            ),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Espaçamento entre períodos
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Substitua APENAS a seção do rodapé (a partir do último HorizontalDivider) por este código:

// RODAPÉ ULTRA COMPACTO - VERSÃO FINAL
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    // LINHA 1: Saldo + Loading Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "💰 Saldo $currentMonth/$currentYear",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "R$ ${"%.2f".format(monthlyBalance)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isExportingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AccentPZ,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // LINHA 2: TODOS OS BOTÕES EM UMA ÚNICA LINHA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // GRÁFICO MENSAL
                        Button(
                            onClick = { showChartScreen = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = WhitePZ
                            ),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text("📊", fontSize = 10.sp)
                        }

                        // GRÁFICO ANUAL
                        Button(
                            onClick = { showAnnualChartScreen = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = WhitePZ
                            ),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text("📈", fontSize = 10.sp)
                        }

                        // MÉDIA ANUAL
                        Button(
                            onClick = { showAnnualAverageDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = WhitePZ
                            ),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text("🧮", fontSize = 10.sp)
                        }

                        // PDF MENSAL
                        Button(
                            onClick = {
                                if (!isExportingPdf) {
                                    scope.launch {
                                        isExportingPdf = true
                                        try {
                                            val monthTransactions = transactions.filter {
                                                it.month == currentMonth && it.year == currentYear
                                            }
                                            val file = pdfExporter.exportMonthlyReport(
                                                monthTransactions,
                                                currentMonth,
                                                currentYear,
                                                monthlyBalance
                                            )
                                            Toast.makeText(
                                                context,
                                                "📄 PDF mensal salvo em Downloads",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "❌ Erro ao gerar PDF",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            isExportingPdf = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isExportingPdf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPZ,
                                contentColor = BlackPZ
                            ),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text("📄", fontSize = 10.sp)
                        }

                        // PDF ANUAL
                        Button(
                            onClick = {
                                if (!isExportingPdf) {
                                    scope.launch {
                                        isExportingPdf = true
                                        try {
                                            val yearTransactions =
                                                transactions.filter { it.year == currentYear }
                                            val file = pdfExporter.exportAnnualReport(
                                                yearTransactions,
                                                currentYear
                                            )
                                            Toast.makeText(
                                                context,
                                                "📋 PDF anual salvo em Downloads",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "❌ Erro ao gerar PDF",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } finally {
                                            isExportingPdf = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isExportingPdf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPZ,
                                contentColor = BlackPZ
                            ),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text("📋", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}