package com.gulderbone.simple_messages.extensions

import androidx.lifecycle.ViewModel

val ViewModel.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }