package com.kunalkulthe.dailyexpenses

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kunalkulthe.dailyexpenses.data.sync.DataSyncWorker
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val name: EditText = findViewById(R.id.editTextName)
        val pass: EditText = findViewById(R.id.editTextPassword)
        val email: EditText = findViewById(R.id.editTextEmail)
        val register: Button = findViewById(R.id.buttonRegister)

        register.setOnClickListener {
            val nameT = name.text.toString()
            val emailT = email.text.toString()
            val passT = pass.text.toString()

            if(nameT == "" || emailT == "" || passT == "")
                Toast.makeText(applicationContext, "Please Enter Data Fields cannot be Empty", Toast.LENGTH_LONG).show()
            else {
                val firebaseDatabase = FirebaseDatabase.getInstance()
                val usersRef = firebaseDatabase.reference.child("users")

                val query = usersRef.orderByChild("email").equalTo(emailT)

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(
                                    applicationContext,
                                    "User Already Registered",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else{
                                val newData = HashMap<String, Any>()
                                newData["password"] = passT
                                newData["email"] = emailT
                                newData["name"] = nameT

                                val newUserRef = usersRef.push()
                                newUserRef.setValue(newData)
                                    .addOnSuccessListener {
                                        val userId = newUserRef.key as String// Retrieve the auto-generated user ID
                                        UserPreferences(applicationContext).storeUserId(userId)
                                        UserPreferences(applicationContext).storeUserName(nameT)
                                        UserPreferences(applicationContext).storeUserEmail(emailT)

                                        Toast.makeText(applicationContext, "Registration Successful", Toast.LENGTH_LONG).show()

                                        val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>().build()
                                        WorkManager.getInstance(applicationContext).enqueue(workRequest)

                                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                        finish()

                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(applicationContext, "Registration Failed", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, "Please Try Again", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }

    }
}