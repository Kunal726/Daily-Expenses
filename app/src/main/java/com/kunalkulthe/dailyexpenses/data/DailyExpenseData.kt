package com.kunalkulthe.dailyexpenses.data

data class DailyExpenseData(
    val date: String,
    val amount: String,
    val expense: List<DailyData>
)
