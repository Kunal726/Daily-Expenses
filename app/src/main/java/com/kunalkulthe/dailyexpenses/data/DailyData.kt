package com.kunalkulthe.dailyexpenses.data

data class DailyData(
    val expId: Long,
    val category: String,
    val description: String,
    val amount: String
)
