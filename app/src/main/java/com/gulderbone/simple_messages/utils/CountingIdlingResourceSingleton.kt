package com.gulderbone.simple_messages.utils

import androidx.test.espresso.idling.CountingIdlingResource

object CountingIdlingResourceSingleton {

    private const val RESOURCE = "GLOBAL"

    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}