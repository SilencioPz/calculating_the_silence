package com.example.calculandoosilencio.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.calculandoosilencio.data.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlin.math.absoluteValue

@Composable
fun ExpenseChart(transactions: List<Transaction>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PieChart(context).apply {
                // Configurações básicas
                description.isEnabled = false
                setUsePercentValues(true)
                setHoleColor(Color.TRANSPARENT)
                setDrawHoleEnabled(true)
                setHoleRadius(40f)
                setTransparentCircleRadius(45f)
                setRotationEnabled(true)
                setTouchEnabled(true)
                legend.isEnabled = true
                animateY(1000)
            }
        },
        update = { chart ->
            if (transactions.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            // CORREÇÃO: Separar TODAS as transações (não filtrar apenas negativas)
            val income = transactions.filter { it.amount > 0 }
            val expenses = transactions.filter { it.amount < 0 }

            val entries = mutableListOf<PieEntry>()

            // CORREÇÃO: Verificar se há entradas E adicionar
            if (income.isNotEmpty()) {
                val totalIncome = income.sumOf { it.amount }.toFloat()
                entries.add(PieEntry(totalIncome, "Entradas"))
            }

            // CORREÇÃO: Verificar se há saídas E adicionar
            if (expenses.isNotEmpty()) {
                val totalExpenses = expenses.sumOf { it.amount }.absoluteValue.toFloat()
                entries.add(PieEntry(totalExpenses, "Saídas"))
            }

            // Se não há dados, mostrar mensagem
            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val dataSet = PieDataSet(entries, "Balanço").apply {
                // CORREÇÃO: Definir cores explicitamente baseado na ordem
                colors = mutableListOf<Int>().apply {
                    if (income.isNotEmpty()) {
                        add(Color.parseColor("#4CAF50")) // Verde para entradas
                    }
                    if (expenses.isNotEmpty()) {
                        add(Color.parseColor("#F44336")) // Vermelho para saídas
                    }
                }

                valueTextColor = Color.BLACK
                valueTextSize = 14f
                sliceSpace = 2f
                selectionShift = 8f
                valueFormatter = PercentFormatter(chart)
            }

            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        }
    )
}