package com.kunalkulthe.dailyexpenses

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kunalkulthe.dailyexpenses.preferences.UserPreferences

class Profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPreferences = UserPreferences(requireContext())

        val username: TextView = view.findViewById(R.id.textViewUsername)
        val email: TextView = view.findViewById(R.id.textViewEmail)
        val userId: TextView = view.findViewById(R.id.textViewUserId)
        val logout: TextView = view.findViewById(R.id.buttonLogout)

        username.text = "Username : ${userPreferences.getUserName()}"
        email.text = "Email : ${userPreferences.getUserEmail()}"
        userId.text = "UserId : ${userPreferences.getUserId()}"

        logout.setOnClickListener {
            userPreferences.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

}