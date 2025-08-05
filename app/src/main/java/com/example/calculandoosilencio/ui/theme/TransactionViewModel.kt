package com.example.calculandoosilencio.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculandoosilencio.data.AppDatabase
import com.example.calculandoosilencio.data.PdfExporter
import com.example.calculandoosilencio.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import kotlin.math.absoluteValue

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.create(application).transactionDao()

    // Fluxos privados
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // Inicia no mês atual
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    // Fluxos públicos (read-only)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    //Fluxos de transações (privados primeiro, read-only em segundo)
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    val annualExpensesByCategory: StateFlow<Map<String, Double>> =
        _transactions.combine(_currentYear) { transactions, year ->
            transactions
                .filter { it.year == year && it.amount < 0 }
                .groupBy { it.category }
                .mapValues { (_, transactions) ->
                    transactions.sumOf { it.amount }.absoluteValue
                }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val annualAverage: StateFlow<Double> =
        _transactions.combine(_currentYear) { transactions, year ->
            val monthlyTotals = transactions
                .filter { it.year == year }
                .groupBy { it.month }
                .mapValues { (_, monthTransactions) ->
                    monthTransactions.sumOf { it.amount }
                }

            if (monthlyTotals.isEmpty()) 0.0
            else monthlyTotals.values.average()
        }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val monthlyBalance: StateFlow<Double> =
        combine(_transactions, _currentMonth, _currentYear) { transactions, month, year ->
            transactions
                .filter { it.month == month && it.year == year }
                .sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    init {
        viewModelScope.launch {
            dao.getAllFlow().collect { transactions ->
                _transactions.value = transactions
            }
        }
    }

    fun setPeriod(month: Int, year: Int) {
        _currentMonth.value = month
        _currentYear.value = year
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val transactionWithPeriod = transaction.copy(
                month = _currentMonth.value,
                year = _currentYear.value
            )

            dao.insert(transactionWithPeriod)
        }
    }

    fun updateTransaction(updatedTransaction: Transaction) {
        viewModelScope.launch {
            dao.update(updatedTransaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction.id)
        }
    }

    fun generateMonthlyReport(context: Context): File {
        val currentTransactions = _transactions.value
            .filter { it.month == _currentMonth.value && it.year == _currentYear.value }

        return PdfExporter(context).exportMonthlyReport(
            transactions = currentTransactions,
            month = _currentMonth.value,
            year = _currentYear.value,
            balance = monthlyBalance.value
        )
    }

    fun generateAnnualReport(context: Context): File {
        val yearTransactions = _transactions.value
            .filter { it.year == _currentYear.value }

        return PdfExporter(context).exportAnnualReport(
            transactions = yearTransactions,
            year = _currentYear.value
        )
    }
}