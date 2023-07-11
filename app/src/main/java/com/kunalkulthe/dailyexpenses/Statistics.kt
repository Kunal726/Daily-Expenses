package com.kunalkulthe.dailyexpenses

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.kunalkulthe.dailyexpenses.data.dao.ExpenseDao
import java.time.LocalDate

class Statistics : Fragment() {

    private val options = listOf("Daily", "Monthly", "Yearly")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner: Spinner = view.findViewById(R.id.timeframeSpinner)
        val pieChart: PieChart = view.findViewById(R.id.pieChart_view)


        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                displayPie(position, pieChart, requireContext().applicationContext)

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onNothingSelected(parent: AdapterView<*>?) {
                displayPie(0, pieChart, requireContext().applicationContext)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayPie(position: Int, pieChart: PieChart, applicationContext: Context) {
        val currentDate = LocalDate.now().dayOfMonth.toString()
        var currentMonth = LocalDate.now().monthValue.toString()
        val currentYear = LocalDate.now().year.toString()
        if(currentMonth.toInt() < 10)
            currentMonth = "0$currentMonth"

        var date = "$currentYear-$currentMonth-$currentDate"

        when (position)
        {
            0 -> {
                val entries = ExpenseDao(applicationContext).getExpenseByDate(date)
                displayPieChart(pieChart, applicationContext, entries)
            }

            1 -> {
                date = "$currentYear-$currentMonth%"
                val entries = ExpenseDao(applicationContext).getExpenseByDate(date)
                displayPieChart(pieChart, applicationContext, entries)
            }

            2 -> {
                date = "$currentYear%"
                val entries = ExpenseDao(applicationContext).getExpenseByDate(date)
                displayPieChart(pieChart, applicationContext, entries)
            }
        }
    }

    private fun displayPieChart(
        pieChart: PieChart,
        applicationContext: Context,
        exp: List<ExpenseByCategory>
    ) {
        pieChart.description.isEnabled = true
        pieChart.description.text = "Expenses"
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        // on below line we are setting drag for our pie chart
        pieChart.dragDecelerationFrictionCoef = 0.95f

        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        // on  below line we are setting hole radius
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        // on below line we are setting center text
        pieChart.setDrawCenterText(true)

        // on below line we are setting
        // rotation for our pie chart
        pieChart.rotationAngle = 0f

        // enable rotation of the pieChart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        // on below line we are setting animation for our pie chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // on below line we are disabling our legend for pie chart
        pieChart.legend.isEnabled = true
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        val entries: ArrayList<PieEntry> = ArrayList()
        for (i in exp)
        {
            val pieEntry = PieEntry(i.total_amount.toFloat(), i.category)
            entries.add(pieEntry)
        }

        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "\nExpenses")

        // on below line we are setting icons.
        dataSet.setDrawIcons(true)

        // on below line we are setting slice for pie
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(applicationContext, R.color.purple_200))
        colors.add(ContextCompat.getColor(applicationContext, R.color.yellow))
        colors.add(ContextCompat.getColor(applicationContext, R.color.red))

        // on below line we are setting colors.
        dataSet.colors = colors

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueTextSize(15f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data

        // undo all highlights
        pieChart.highlightValues(null)

        // loading chart
        pieChart.invalidate()

    }

}