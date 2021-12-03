package com.gulderbone.simple_messages.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.databinding.FragmentMainBinding

class MainFragment : BaseFragment<FragmentMainBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding =
        FragmentMainBinding::inflate

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            navController.navigate(R.id.action_mainFragment_to_registerFragment)
        } else {
            navController.navigate(R.id.action_mainFragment_to_latestMessagesFragment)
        }
    }
}