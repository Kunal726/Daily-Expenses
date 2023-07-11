package com.kunalkulthe.dailyexpenses.data.sync

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.kunalkulthe.dailyexpenses.data.Expense
import com.kunalkulthe.dailyexpenses.data.dao.ExpenseDao
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class DataSyncWorker(appContext: Context, workerParameters: WorkerParameters) : CoroutineWorker(appContext, workerParameters){
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val userId = UserPreferences(applicationContext).getUserId().toString()
            if (isConnectedToInternet()) {
                synchronizeSQLiteToFirebase(userId)
                synchronizeFirebaseToSQLite(userId)
            } else {
                Result.retry()
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("Sync", "${e.message}")
            Result.failure()
        }
    }

    private fun synchronizeFirebaseToSQLite(userId: String) {
        val expenseDao = ExpenseDao(applicationContext)
        val firebaseDatabase = FirebaseDatabase.getInstance().reference.child("expenses")
        val expenseDb = firebaseDatabase.child(userId)

        expenseDb.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children)
                {
                    val expenseId = childSnapshot.key.toString().toLong()
                    Log.e("JSON",childSnapshot.value.toString())
                    val expenseMap = childSnapshot.getValue<Map<String, Any>>()
                    if(expenseMap != null)
                    {
                        val expense = Expense(
                            expId = expenseId,
                            userId = userId,
                            category = expenseMap["category"] as String,
                            description = expenseMap["description"] as String,
                            amount = expenseMap["amount"] as Long,
                            date = expenseMap["date"] as String,
                            isSynced = expenseMap["isSynced"] as Boolean,
                            image = Base64.decode(expenseMap["image"].toString(), Base64.DEFAULT)
                        )

                        val existingExpense = expenseDao.getExpenseById(expenseId)
                        if (existingExpense == null)
                            expenseDao.insertExpense(expense.copy(isSynced = true))
                        else
                            expenseDao.updateExpense(expense.copy(isSynced = true))
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Sync Firebase","Firebase synchronization cancelled: ${error.message}")
            }

        })
    }

    private fun synchronizeSQLiteToFirebase(userId: String) {
        val expenseDao = ExpenseDao(applicationContext)
        val unsyncedExpenses = expenseDao.getAllExpenses()

        if (unsyncedExpenses.isNotEmpty())
        {
            val firebaseDatabase = FirebaseDatabase.getInstance().reference.child("expenses")
            val userExpense = firebaseDatabase.child(userId)
            for(expense in unsyncedExpenses)
            {
                val newExpense = HashMap<String, Any>()
                newExpense["amount"] = expense.amount
                newExpense["category"] = expense.category
                newExpense["description"] = expense.description
                newExpense["date"] = expense.date
                newExpense["image"] = Base64.encodeToString(expense.image, Base64.DEFAULT)
                newExpense["isSynced"] = expense.isSynced

                userExpense.child(expense.expId.toString()).updateChildren(newExpense)
                    .addOnSuccessListener {
                        Log.e("Add", "Success")
                        expenseDao.updateExpense(expense.copy(isSynced = true))
                    }
                    .addOnFailureListener { Log.e("Add", "Fail") }
            }
        }
    }

    private fun isConnectedToInternet(): Boolean {
        return try {
            val ipAddr: InetAddress = InetAddress.getByName("google.com")
            // Return true if the device can successfully resolve the host
            !ipAddr.equals("")
        } catch (e: Exception) {
            // Return false if an exception occurs while resolving the host
            false
        }
    }
}