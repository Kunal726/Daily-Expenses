package com.kunalkulthe.dailyexpenses

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private val SPLASH_DELAY = 5000L // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Define the periodic interval for the worker
        val repeatIntervalMinutes = 15L

        // Create a periodic work request
        val dataWorkRequest = PeriodicWorkRequest.Builder(
            DataSyncWorker::class.java,
            repeatIntervalMinutes,
            TimeUnit.MINUTES
        ).build()

        WorkManager
            .getInstance(this.applicationContext)
            .enqueue(dataWorkRequest)

        // Delay for 5 seconds and then start MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMainActivity()
        }, SPLASH_DELAY)

    }

    private fun navigateToMainActivity() {
        val userPreferences = UserPreferences(applicationContext) // Implement your logic to check if the user is logged in

        if (userPreferences.isLoggedIn()) {
            // User is already logged in, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // User is not logged in, navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        finish()
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }
}