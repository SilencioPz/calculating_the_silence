package com.example.calculandoosilencio.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val month: Int,
    val year: Int
) {
    // Construtor conveniente para criar transações com data atual
    constructor(
        description: String,
        amount: Double,
        category: String,
        month: Int,
        year: Int
    ) : this(
        id = UUID.randomUUID().toString(),
        description = description,
        amount = amount,
        category = category,
        date = System.currentTimeMillis(),
        month = month,
        year = year
    )
}