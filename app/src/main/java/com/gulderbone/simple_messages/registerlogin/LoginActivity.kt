package com.gulderbone.simple_messages.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.databinding.ActivityLoginBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.presentation.latestmessages.LatestMessagesActivity

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButtonLogin.setOnClickListener {
            val email = binding.emailEdittextLogin.editText?.text.toString()
            val password = binding.passwordEdittextLogin.editText?.text.toString()

            Log.d(TAG, "Attempt login with email/pw: $email/***")

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to log in: $it")
                }
        }

        binding.backToRegistrationTextviewLogin.setOnClickListener {
            finish()
        }
    }
}