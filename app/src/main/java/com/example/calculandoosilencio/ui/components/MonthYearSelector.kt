package com.example.calculandoosilencio.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import java.time.Month
import java.util.*

@Composable
fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val months = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril",
        "Maio", "Junho", "Julho", "Agosto",
        "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Seletor de Mês
        var expandedMonth by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { expandedMonth = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(months[selectedMonth - 1])
            }
            DropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }
            ) {
                months.forEachIndexed { index, monthName ->
                    DropdownMenuItem(
                        text = { Text(monthName) },
                        onClick = {
                            onMonthChange(index + 1)
                            expandedMonth = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        var expandedYear by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { expandedYear = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedYear.toString())
            }
            DropdownMenu(
                expanded = expandedYear,
                onDismissRequest = { expandedYear = false }
            ) {
                (2020..2030).forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearChange(year)
                            expandedYear = false
                        }
                    )
                }
            }
        }
    }
}