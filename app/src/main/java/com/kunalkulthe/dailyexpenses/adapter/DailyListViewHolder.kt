package com.kunalkulthe.dailyexpenses.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kunalkulthe.dailyexpenses.R
import com.kunalkulthe.dailyexpenses.data.DailyData

class DailyListViewHolder(itemView: View, onItemClick: (position: Int) -> Unit): RecyclerView.ViewHolder(itemView) {
    private val amount = itemView.findViewById<TextView>(R.id.recycle_amount_daily)
    private val description = itemView.findViewById<TextView>(R.id.recycle_description)
    private val category = itemView.findViewById<TextView>(R.id.recycle_category)

    init {
        itemView.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Pass the position to the callback method
                onItemClick(position)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(data: DailyData)
    {
        amount.text = "₹ ${data.amount}"
        description.text = data.description
        category.text = data.category
    }

}