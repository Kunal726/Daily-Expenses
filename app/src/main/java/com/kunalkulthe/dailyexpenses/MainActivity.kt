package com.kunalkulthe.dailyexpenses

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        syncData()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val fragment = DailyExpense()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.action_daily_expense

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId)
            {
                R.id.action_daily_expense -> navigateToDailyExpenseFragment()
                R.id.action_statistics -> navigateToStatisticsFragment()
                R.id.action_profile -> navigateToProfileFragment()
            }
            true
        }

    }

    private fun syncData() {
        val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    private fun navigateToDailyExpenseFragment() {
        val fragment = DailyExpense()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToStatisticsFragment() {
        val fragment = Statistics()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun navigateToProfileFragment() {
        val fragment = Profile()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


}