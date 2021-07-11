package com.gulderbone.simple_messages.extensions

import androidx.appcompat.app.AppCompatActivity

val AppCompatActivity.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }