package com.gulderbone.simple_messages.models

data class PushNotification(
    val data: NotificationData,
    val receiverMessagingToken: String
)
