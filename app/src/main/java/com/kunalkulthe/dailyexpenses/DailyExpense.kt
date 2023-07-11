package com.kunalkulthe.dailyexpenses

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kunalkulthe.dailyexpenses.adapter.DailyExpensesAdapter
import com.kunalkulthe.dailyexpenses.adapter.MonthSpinnerAdapter
import com.kunalkulthe.dailyexpenses.data.dao.ExpenseDao
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker
import com.kunalkulthe.dailyexpenses.listener.CustomSpinnerListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class DailyExpense : Fragment(), CustomSpinnerListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var months: ArrayList<String>
    private lateinit var total_exp: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily_expense, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.visibility = View.VISIBLE


        total_exp = view.findViewById(R.id.total_Expense)

        var currentMonth = LocalDate.now().monthValue.toString()
        val currentYear = LocalDate.now().year.toString()
        val currentDay = LocalDate.now().dayOfMonth.toString()
        if(currentMonth.toInt() < 10)
            currentMonth = "0$currentMonth"

        val date = "$currentYear-$currentMonth%"

        recyclerView = view.findViewById(R.id.details_recycle_daily)

        months = ExpenseDao(requireContext().applicationContext).getMonths() as ArrayList<String>

        val MonthandYear = ArrayList<String>()

        val month_curr = getMonthAndYearFromDate("$currentYear-$currentMonth-$currentDay")
        for (i in months)
        {
            MonthandYear.add(getMonthAndYearFromDate(i))
        }
        if (MonthandYear.indexOf(month_curr) < 0 )
        {
            total_exp.text = "₹ 0"
            months.add("${date.subSequence(0, date.length - 1)}-$currentDay")
        }
        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.add_expense)

        val spinner: MonthSpinnerAdapter = view.findViewById(R.id.spinMonth)
        spinner.setItems(MonthandYear, month_curr)
        spinner.setListener(this)

        displayData(recyclerView, requireContext().applicationContext, date)



        floatingActionButton.setOnClickListener {
            val addExpenseFragment = AddExpense()
            // Get the FragmentManager
            val fragmentManager = requireActivity().supportFragmentManager

            // Start a FragmentTransaction
            val fragmentTransaction = fragmentManager.beginTransaction()

            // Replace the current fragment with the AddExpenseFragment
            fragmentTransaction.replace(R.id.fragment_container, addExpenseFragment)

            // Add the transaction to the back stack
            fragmentTransaction.addToBackStack(null)

            // Commit the transaction
            fragmentTransaction.commit()
        }

        view.findViewById<ImageButton>(R.id.sync_data).setOnClickListener {
            Toast.makeText(
                requireContext().applicationContext,
                "Syncing Data",
                Toast.LENGTH_SHORT
            ).show()
            syncData()
            onViewCreated(view, savedInstanceState)
        }

    }

    private fun displayData(recyclerView: RecyclerView, applicationContext: Context, date: String) {
        recyclerView.adapter = null
        val expenses = ExpenseDao(applicationContext).getMonthExpenses(date)
        val layoutManager = LinearLayoutManager(requireContext().applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = DailyExpensesAdapter(expenses, requireContext().applicationContext, requireActivity())
    }

    private fun getMonthAndYearFromDate(dateString: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateString)

        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }

        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        val year = calendar.get(Calendar.YEAR)

        return "$month $year"
    }

    @SuppressLint("SetTextI18n")
    override fun onLeftButtonClick(currentPosition: Int) {
        displayData(recyclerView, requireContext().applicationContext, months[currentPosition].substring(0, months[currentPosition].length - 3) + "%")
        val total = ExpenseDao(requireContext().applicationContext).getTotalExpenses(months[currentPosition].substring(0, months[currentPosition].length - 3) + "%")
        total_exp.text = "₹ $total"
    }

    @SuppressLint("SetTextI18n")
    override fun onRightButtonClick(currentPosition: Int) {
        displayData(recyclerView, requireContext().applicationContext, months[currentPosition].substring(0, months[currentPosition].length - 3) + "%")
        val total = ExpenseDao(requireContext().applicationContext).getTotalExpenses(months[currentPosition].substring(0, months[currentPosition].length - 3) + "%")
        total_exp.text = "₹ $total"
    }

    private fun syncData() {
        val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>().build()
        WorkManager.getInstance(requireContext().applicationContext).enqueue(workRequest)
    }
}