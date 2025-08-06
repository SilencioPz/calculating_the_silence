package com.example.calculandoosilencio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.calculandoosilencio.data.Transaction
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.absoluteValue

@Composable
fun AnnualChart(transactions: List<Transaction>) {
    println("DEBUG: AnnualChart iniciado com ${transactions.size} transações")

    var showPieChart by remember { mutableStateOf(true) }

    // Log das transações para debug
    LaunchedEffect(transactions) {
        println("DEBUG: Transações recebidas:")
        transactions.forEach {
            println("  - ${it.description}: R$ ${it.amount} (${it.category})")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Botões de seleção do tipo de gráfico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    showPieChart = true
                    println("DEBUG: Mudando para BarChart")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showPieChart) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (showPieChart) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Gráfico Pizza")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    showPieChart = false
                    println("DEBUG: Mudando para BarChart")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!showPieChart) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!showPieChart) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Gráfico Barra")
            }
        }

        // Área do gráfico
        Box(modifier = Modifier.fillMaxSize()) {
            if (showPieChart) {
                PieChartAnnual(transactions)
            } else {
                BarChartAnnual(transactions)
            }
        }
    }
}

@Composable
private fun PieChartAnnual(transactions: List<Transaction>) {
    println("DEBUG: PieChartAnnual chamado")

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(false)
                legend.isEnabled = true
                setDrawEntryLabels(true)
                setEntryLabelTextSize(12f)
                animateY(1000)
                setEntryLabelColor(android.graphics.Color.WHITE) // Adiciona texto branco
                println("DEBUG: PieChart criado")
            }
        },
        update = { chart ->
            try {
                // Agrupar por categoria (tanto entradas quanto saídas)
                val dataByCategory = transactions
                    .groupBy { it.category }
                    .mapValues { (_, transactions) ->
                        transactions.sumOf { it.amount }
                    }
                    .filter { it.value != 0.0 }

                println("DEBUG: Dados por categoria: $dataByCategory")

                if (dataByCategory.isEmpty()) {
                    println("DEBUG: Nenhum dado encontrado")
                    return@AndroidView
                }

                val entries = dataByCategory.map { (category, amount) ->
                    PieEntry(amount.absoluteValue.toFloat(), category)
                }

                val dataSet = PieDataSet(entries, "Entradas e Saídas por Categoria").apply {
                    // Cores diferentes para entradas (verde) e saídas (vermelho)
                    colors = dataByCategory.map { (_, amount) ->
                        if (amount >= 0) {
                            android.graphics.Color.parseColor("#4CAF50") // Verde para entradas
                        } else {
                            android.graphics.Color.parseColor("#F44336") // Vermelho para saídas
                        }
                    }
                    valueTextColor = android.graphics.Color.WHITE
                    valueTextSize = 12f
                    sliceSpace = 3f
                    selectionShift = 5f
                }

                val data = PieData(dataSet).apply {
                    setValueFormatter(LargeValueFormatter())
                }

                chart.data = data
                chart.invalidate()

                println("DEBUG: PieChart atualizado com ${entries.size} entradas")

            } catch (e: Exception) {
                println("DEBUG: Erro no PieChart: ${e.message}")
                e.printStackTrace()
            }
        }
    )
}

@Composable
private fun BarChartAnnual(transactions: List<Transaction>) {
    println("DEBUG: BarChartAnnual chamado")

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = true
                animateY(1000)

                // Configurar cores do texto
                legend.textColor = android.graphics.Color.WHITE
                xAxis.textColor = android.graphics.Color.WHITE
                axisLeft.textColor = android.graphics.Color.WHITE
                axisRight.textColor = android.graphics.Color.WHITE

                // Configurar eixos
                xAxis.apply {
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    axisMinimum = 0f
                }

                axisRight.isEnabled = false

                println("DEBUG: BarChart criado")
            }
        },
        update = { chart ->
            try {
                // Agrupar por mês e separar entradas/saídas
                val monthlyData = (1..12).map { month ->
                    val monthTransactions = transactions.filter { it.month == month }
                    val income = monthTransactions.filter { it.amount > 0 }.sumOf { it.amount }
                    val expense = monthTransactions.filter { it.amount < 0 }.sumOf { it.amount }.absoluteValue

                    Triple(month, income.toFloat(), expense.toFloat())
                }

                println("DEBUG: Dados mensais calculados")

                val entriesIncome = monthlyData.mapIndexed { index, (_, income, _) ->
                    BarEntry(index.toFloat(), income)
                }

                val entriesExpense = monthlyData.mapIndexed { index, (_, _, expense) ->
                    BarEntry(index.toFloat(), expense)
                }

                // CORREÇÃO: Cores corretas - Verde para entradas, Vermelho para saídas
                val setIncome = BarDataSet(entriesIncome, "Entradas").apply {
                    color = android.graphics.Color.parseColor("#4CAF50") // VERDE para entradas
                    valueTextSize = 10f
                    valueTextColor = android.graphics.Color.WHITE
                }

                val setExpense = BarDataSet(entriesExpense, "Saídas").apply {
                    color = android.graphics.Color.parseColor("#F44336") // VERMELHO para saídas
                    valueTextSize = 10f
                    valueTextColor = android.graphics.Color.WHITE
                }

                val data = BarData(setIncome, setExpense).apply {
                    barWidth = 0.35f
                    setValueFormatter(LargeValueFormatter())
                }

                chart.data = data
                chart.groupBars(0f, 0.1f, 0f) // Agrupar barras lado a lado

                chart.xAxis.valueFormatter = IndexAxisValueFormatter(
                    listOf(
                        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
                    )
                )

                chart.invalidate()

                println("DEBUG: BarChart atualizado - VERDE=Entradas, VERMELHO=Saídas")

            } catch (e: Exception) {
                println("DEBUG: Erro no BarChart: ${e.message}")
                e.printStackTrace()
            }
        }
    )
}