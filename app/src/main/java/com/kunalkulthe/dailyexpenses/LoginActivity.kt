package com.kunalkulthe.dailyexpenses

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences

class LoginActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userPreferences = UserPreferences(applicationContext)

        val login: Button = findViewById(R.id.btnLogin)
        val register: Button = findViewById(R.id.btnRegister)
        val forgot: TextView = findViewById(R.id.textForgotPassword)

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgot.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        login.setOnClickListener { login(findViewById(R.id.editEmail), findViewById(R.id.editPassword)) }

    }

    private fun login(email: EditText?, password: EditText?) {
        val emailStr = email?.text.toString()
        val passStr = password?.text.toString()

        if(emailStr == "" || passStr == "")
            Toast.makeText(applicationContext, "Email/Password cannot be Empty", Toast.LENGTH_LONG).show()
        else {

            val usersRef = FirebaseDatabase.getInstance().reference.child("users")
            val query = usersRef.orderByChild("email").equalTo(emailStr)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.value as? Map<*, *>

                            if (userData != null && userData["password"] == passStr) {
                                val userId = userSnapshot.key as String
                                val name = userData["name"] as String

                                userPreferences.storeUserId(userId)
                                userPreferences.storeUserName(name)
                                userPreferences.storeUserEmail(emailStr)

                                val workRequest =
                                    OneTimeWorkRequestBuilder<DataSyncWorker>().build()
                                WorkManager.getInstance(applicationContext).enqueue(workRequest)

                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()

                                break
                            }
                        }
                    }else{
                        Toast.makeText(
                            applicationContext,
                            "User Not Registered",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(applicationContext, "Unable To Login Please Try Again", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}