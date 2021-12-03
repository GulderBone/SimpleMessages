package com.gulderbone.simple_messages.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.databinding.ActivityMainBinding
import com.gulderbone.simple_messages.extensions.visibleOrGone

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.actionBarTitle().observe(this) {
            supportActionBar?.title = it
        }

        mainViewModel.loaderVisibility().observe(this) {
            binding.loader.loader.visibleOrGone(it)
        }
    }
}