package com.kunalkulthe.dailyexpenses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kunalkulthe.dailyexpenses.R
import com.kunalkulthe.dailyexpenses.data.DailyData

class DailyListAdapter(private val dataList: List<DailyData>, private val onItemClick: (position: Int) -> Unit): RecyclerView.Adapter<DailyListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_daily_recycle_layout, parent, false)
        return DailyListViewHolder(itemView, onItemClick)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: DailyListViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data)
    }
}