package com.gulderbone.simple_messages.registerlogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.databinding.FragmentLoginBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.extensions.visibleOrGone
import com.gulderbone.simple_messages.main.MainViewModel

class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding =
        FragmentLoginBinding::inflate

    private lateinit var navController: NavController

    private val baseViewModel by lazy { ViewModelProvider(requireActivity()).get(MainViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.loginButtonLogin.setOnClickListener {
            val email = binding.emailEdittextLogin.editText?.text.toString()
            val password = binding.passwordEdittextLogin.editText?.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(requireActivity(), "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (password.isEmpty()) {
                Toast.makeText(requireActivity(), "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempt login with email/pw: $email/***")

            baseViewModel.setLoaderVisibility(true)

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    navController.navigate(R.id.action_loginFragment_to_latestMessagesFragment)
                }
                .addOnFailureListener {
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Failed to log in: $it")
                    baseViewModel.setLoaderVisibility(false)
                }
        }

        binding.backToRegistrationTextviewLogin.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        baseViewModel.loaderVisibility().observe(viewLifecycleOwner) {
            binding.loader.loader.visibleOrGone(it)
        }
    }
}