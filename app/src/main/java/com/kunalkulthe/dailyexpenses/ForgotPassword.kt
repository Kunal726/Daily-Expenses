package com.kunalkulthe.dailyexpenses

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val userPreferences = UserPreferences(applicationContext)

        val email: EditText = findViewById(R.id.edit_email)
        val newPass: EditText = findViewById(R.id.edit_new_password)

        val reset: Button = findViewById(R.id.button_reset_password)
        val submit: Button = findViewById(R.id.button_submit)

        reset.setOnClickListener {
            val emailId = email.text.toString()

            if (emailId == "") Toast.makeText(applicationContext, "Enter Email To Reset", Toast.LENGTH_LONG).show()
            else{
                email.isEnabled = false
                val database = FirebaseDatabase.getInstance()
                val usersRef = database.reference.child("users")

                val query = usersRef.orderByChild("email").equalTo(emailId)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {

                            for (userSnapshot in dataSnapshot.children) {
                                val userId = userSnapshot.key as String
                                val name = userSnapshot.child("name").value as String

                                userPreferences.storeUserId(userId)
                                userPreferences.storeUserName(name)

                                newPass.visibility = View.VISIBLE
                                submit.visibility = View.VISIBLE

                            }
                        } else {
                            email.isEnabled = true
                            Toast.makeText(applicationContext, "Email Not Registered please Register First", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this@ForgotPassword, RegisterActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(applicationContext, "Some Error Occured \nPlease Retry After Some Time", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ForgotPassword, LoginActivity::class.java))
                        finish()
                    }
                })
            }
        }

        submit.setOnClickListener {
            if(newPass.text.toString() == "")
                Toast.makeText(applicationContext, "Please Enter New Password", Toast.LENGTH_LONG).show()
            else{
                val firebaseDatabase = FirebaseDatabase.getInstance()
                val usersRef = firebaseDatabase.getReference("users").child(""+userPreferences.getUserId())

                val newData = HashMap<String, Any>()
                newData["password"] = newPass.text.toString()

                usersRef.updateChildren(newData)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Password Re-set Successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ForgotPassword, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(applicationContext, "Password Re-set Failed Retry Again", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@ForgotPassword, LoginActivity::class.java))
                        finish()
                    }
            }
        }

    }
}