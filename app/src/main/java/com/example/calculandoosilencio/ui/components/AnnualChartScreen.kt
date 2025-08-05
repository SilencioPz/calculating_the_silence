package com.example.calculandoosilencio.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.calculandoosilencio.data.Transaction
import kotlin.math.absoluteValue

@Composable
fun AnnualChartScreen(
    key: String,
    transactions: List<Transaction>,
    selectedYear: Int,
    onBack: () -> Unit,
    annualExpenses: Map<String, Double>
) {
    println("DEBUG: AnnualChartScreen iniciada com ${transactions.size} transações")

    // BackHandler para interceptar o botão voltar do sistema
    BackHandler {
        println("DEBUG: BackHandler ativado")
        onBack()
    }

    val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }.absoluteValue

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header com botão voltar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        println("DEBUG: Botão VOLTAR clicado")
                        onBack()
                    },
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("VOLTAR")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    "Gráfico Anual $selectedYear",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Conteúdo scrollável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Card com resumo financeiro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Resumo Anual $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Total Entradas: R$ ${"%.2f".format(totalIncome)}",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Total Saídas: R$ ${"%.2f".format(totalExpense)}",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Saldo: R$ ${"%.2f".format(totalIncome - totalExpense)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (totalIncome - totalExpense >= 0) Color.Green else Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Área do gráfico
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Gráfico de Gastos por Categoria",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp) // Altura fixa para o gráfico
                    ) {
                        if (transactions.isEmpty()) {
                            Text(
                                text = "Nenhuma transação registrada em $selectedYear",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            println("DEBUG: Chamando AnnualChart com ${transactions.size} transações")
                            AnnualChart(transactions = transactions)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detalhamento por categoria (usando annualExpenses)
            if (annualExpenses.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Gastos por Categoria",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        annualExpenses.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "R$ ${"%.2f".format(amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}