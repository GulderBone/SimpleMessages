package com.gulderbone.simple_messages.extensions

import android.app.Service

val Service.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }