package com.kunalkulthe.dailyexpenses.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kunalkulthe.dailyexpenses.R
import com.kunalkulthe.dailyexpenses.listener.CustomSpinnerListener

class MonthSpinnerAdapter(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val btnLeft: ImageButton
    private val btnRight: ImageButton
    private val txtValue: TextView

    private var itemList: List<String> = listOf()
    private var currentPosition: Int = 0
    private var listener: CustomSpinnerListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_spinner, this, true)

        btnLeft = findViewById(R.id.spinLeft)
        btnRight = findViewById(R.id.spinRight)
        txtValue = findViewById(R.id.spinText)

        btnLeft.setOnClickListener {
            currentPosition--
            if (currentPosition < 0) {
                currentPosition = itemList.size - 1
            }
            txtValue.text = itemList[currentPosition]
            listener?.onLeftButtonClick(currentPosition)
        }

        btnRight.setOnClickListener {
            currentPosition++
            if (currentPosition == itemList.size) {
                currentPosition = 0
            }
            txtValue.text = itemList[currentPosition]
            listener?.onRightButtonClick(currentPosition)
        }
    }

    fun setItems(items: ArrayList<String>, month_curr: String) {
        currentPosition = 0

        currentPosition = if(items.contains(month_curr)) {
            items.indexOf(month_curr).toInt()
        } else {
            items.add(month_curr)
            items.size - 1
        }
        itemList = items
        txtValue.text = itemList[currentPosition]
    }

    fun setListener(listener: CustomSpinnerListener) {
        this.listener = listener
    }
}