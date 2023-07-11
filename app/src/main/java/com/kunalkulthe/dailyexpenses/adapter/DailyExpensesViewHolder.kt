package com.kunalkulthe.dailyexpenses.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kunalkulthe.dailyexpenses.MainActivity
import com.kunalkulthe.dailyexpenses.R
import com.kunalkulthe.dailyexpenses.UpdateExpense
import com.kunalkulthe.dailyexpenses.data.DailyExpenseData

class DailyExpensesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val date: TextView = itemView.findViewById(R.id.recycle_date)
    private val amount: TextView = itemView.findViewById(R.id.recycle_expense)
    val recycleDailyList = itemView.findViewById<RecyclerView>(R.id.recycle_dailyList)


    @SuppressLint("SetTextI18n")
    fun bind(data: DailyExpenseData, activity: Activity) {
        date.text = data.date
        amount.text = "₹ ${data.amount}"
        recycleDailyList.adapter = DailyListAdapter(dataList = data.expense) { position ->
            val fragment = UpdateExpense()
            val bundle = Bundle()
            bundle.putString("expId", "${data.expense[position].expId}")
            fragment.arguments = bundle

            (activity as MainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}