package com.kunalkulthe.dailyexpenses.data

data class Expense(
    val expId: Long,
    val userId: String,
    val category: String,
    val description: String,
    val amount: Long,
    val date: String,
    val isSynced: Boolean,
    val image: ByteArray
)
