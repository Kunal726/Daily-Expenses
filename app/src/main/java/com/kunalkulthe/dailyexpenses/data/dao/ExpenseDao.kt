package com.kunalkulthe.dailyexpenses.data.dao

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.kunalkulthe.dailyexpenses.ExpenseByCategory
import com.kunalkulthe.dailyexpenses.data.DailyData
import com.kunalkulthe.dailyexpenses.data.DailyExpenseData
import com.kunalkulthe.dailyexpenses.data.Expense
import com.kunalkulthe.dailyexpenses.data.access.DbHelper
import org.json.JSONArray
import org.json.JSONObject

class ExpenseDao (context: Context) {
    var dbHelper : DbHelper
    init {
        dbHelper = DbHelper(context)
    }

    fun getAllExpenses(): List<Expense>
    {
        val query = "SELECT * FROM expenses"
        val cursor = dbHelper.getReadableDb().rawQuery(query, null)
        val expenses: ArrayList<Expense> = arrayListOf()

        cursor.moveToFirst()
        while (!cursor.isAfterLast)
        {
            val expId = cursor.getLong(0)
            val userId = cursor.getString(1)
            val category = cursor.getString(2)
            val description = cursor.getString(3)
            val amount = cursor.getLong(4)
            val date = cursor.getString(5)
            val isSynced = cursor.getInt(6) == 1
            val image = cursor.getBlob(7)

            expenses.add(Expense(expId, userId, category, description, amount, date, isSynced, image))


            cursor.moveToNext()
        }

        cursor.close()

        return expenses
    }

    fun insertExpense(expense: Expense)
    {
        val db = dbHelper.getWritableDb()

        val values = ContentValues().apply {
            put("userId", expense.userId)
            put("category", expense.category)
            put("description", expense.description)
            put("amount", expense.amount)
            put("date", expense.date)
            put("isSynced", if (expense.isSynced) 1 else 0)
            put("image", expense.image)
        }

        db.insert("expenses", null, values)

        db.close()
    }

    fun updateExpense(expense: Expense)
    {
        val db = dbHelper.getWritableDb()

        val values = ContentValues().apply {
            put("userId", expense.userId)
            put("category", expense.category)
            put("description", expense.description)
            put("amount", expense.amount)
            put("date", expense.date)
            put("isSynced", if (expense.isSynced) 1 else 0)
            put("image", expense.image)
        }

        val selection = "expId = ?"
        val selectionArgs = arrayOf(expense.expId.toString())

        db.update("expenses", values, selection, selectionArgs)

        db.close()
    }

    fun getExpenseById(expId: Long): Expense?
    {
        val query = "SELECT * FROM expenses WHERE expId = $expId"
        val cursor = dbHelper.getReadableDb().rawQuery(query, null)

        if(cursor.moveToFirst())
        {
            val userId = cursor.getString(1)
            val category = cursor.getString(2)
            val description = cursor.getString(3)
            val amount = cursor.getLong(4)
            val date = cursor.getString(5)
            val isSynced = cursor.getInt(6) == 1
            val image = cursor.getBlob(7)

            return Expense(expId, userId, category, description, amount, date, isSynced, image)
        }

        return null
    }

    fun getExpenseByDate(date: String): List<ExpenseByCategory>
    {
        val query = "SELECT category, SUM(amount) as total_amount FROM expenses where date LIKE '$date' GROUP BY category"
        val cursor = dbHelper.getReadableDb().rawQuery(query, null)
        val expenses: ArrayList<ExpenseByCategory> = arrayListOf()

        cursor.moveToFirst()
        while (!cursor.isAfterLast)
        {
            val category = cursor.getString(0)
            val amount = cursor.getLong(1)

            expenses.add(ExpenseByCategory(category, amount))

            cursor.moveToNext()
        }

        cursor.close()

        return expenses
    }

    fun getMonthExpenses(month: String): List<DailyExpenseData> {
        val dailyData = ArrayList<DailyExpenseData>()
        val query = "SELECT date FROM expenses WHERE date LIKE '$month'"
        var cursor = dbHelper.getReadableDb().rawQuery(query, null)

        val json = JSONObject()

        if (cursor.count > 0 || cursor != null)
        {
            cursor.moveToFirst()
            while (!cursor.isAfterLast)
            {
                json.put(cursor.getString(0), JSONArray())
                cursor.moveToNext()
            }
            cursor.close()
        }

        if(json.length() > 0)
        {
            val query1 = "SELECT expId, amount, description, date, (SELECT SUM(amount) FROM expenses WHERE date = e.date) as total, category FROM expenses e WHERE date LIKE '$month' ORDER BY date DESC"
            cursor = dbHelper.getReadableDb().rawQuery(query1, null)

            if (cursor.count > 0 || cursor != null)
            {
                cursor.moveToFirst()
                while (!cursor.isAfterLast)
                {
                    val obj = JSONObject()
                    obj.put("expId", cursor.getLong(0))
                    obj.put("amount", cursor.getLong(1).toString())
                    obj.put("description", cursor.getString(2))
                    obj.put("date", cursor.getString(3))
                    obj.put("total", cursor.getLong(4))
                    obj.put("category", cursor.getString(5))

                    json.getJSONArray((cursor.getString(3))).put(obj)
                    cursor.moveToNext()
                }
                cursor.close()
            }

            val keys = json.keys()
            while (keys.hasNext()) {
                val date = keys.next().toString()
                val data = json.getJSONArray(date)
                val total_amount = data.getJSONObject(0).getString("total")
                val dailyList = ArrayList<DailyData>()
                for (i in 0 until data.length())
                {
                    val jSONObject = data.getJSONObject(i)
                    dailyList.add(
                        DailyData(expId = jSONObject.getLong("expId"),
                            category = jSONObject.getString("category"),
                            description = jSONObject.getString("description"),
                            amount = jSONObject.getString("amount"))
                    )
                }
                dailyData.add(DailyExpenseData(
                    date = date,
                    amount = total_amount,
                    expense = dailyList
                )
                )

            }

            return dailyData
        }

        Log.e("Json", json.toString())

        return emptyList()
    }

    fun getMonths(): List<String> {
        val months = ArrayList<String>()

        val query = "SELECT date FROM expenses GROUP BY date ORDER BY date ASC"
        val cursor = dbHelper.getReadableDb().rawQuery(query, null)


        if (cursor.count > 0 || cursor != null)
        {
            cursor.moveToFirst()
            while (!cursor.isAfterLast)
            {
                val dateIndex = cursor.getColumnIndex("date")
                val date = cursor.getString(dateIndex).toString()

                months.add(date)
                cursor.moveToNext()
            }
            cursor.close()
            return  months
        }

        return emptyList()
    }

    fun deleteExpense(expense: Expense) {
        val db = dbHelper.getWritableDb()

        val selection = "expId = ?"
        val selectionArgs = arrayOf(expense.expId.toString())

        db.delete("expenses", selection, selectionArgs)

        db.close()
    }

    fun getTotalExpenses(date: String): String {
        val query = "SELECT SUM(amount) as total FROM expenses where date LIKE '$date'"
        val cursor = dbHelper.getReadableDb().rawQuery(query, null)
        var amount: Long = 0

        if (cursor.count > 0 || cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                amount = cursor.getLong(0)
                cursor.moveToNext()
            }
            return amount.toString()
        }

        return "0"
    }
}