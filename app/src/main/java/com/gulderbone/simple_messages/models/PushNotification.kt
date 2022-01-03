package com.gulderbone.simple_messages.models

data class PushNotification(
    val data: NotificationData,
    val to: String
)
