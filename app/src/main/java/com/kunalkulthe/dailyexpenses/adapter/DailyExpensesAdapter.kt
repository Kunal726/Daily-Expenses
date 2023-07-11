package com.kunalkulthe.dailyexpenses.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kunalkulthe.dailyexpenses.R
import com.kunalkulthe.dailyexpenses.data.DailyExpenseData

class DailyExpensesAdapter(private val dataList: List<DailyExpenseData>, private val context: Context, private val activity: Activity): RecyclerView.Adapter<DailyExpensesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyExpensesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.details_recycle_layout, parent, false)
        return DailyExpensesViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: DailyExpensesViewHolder, position: Int) {
        val data = dataList[position]
        holder.recycleDailyList.layoutManager = LinearLayoutManager(context)
        holder.bind(data, activity)
    }
}