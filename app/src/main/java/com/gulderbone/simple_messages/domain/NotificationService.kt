package com.gulderbone.simple_messages.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.main.MainActivity
import com.gulderbone.simple_messages.models.User
import kotlin.random.Random

private const val MESSAGE_CHANNEL_ID = "messages"
private const val MESSAGE_CHANNEL_NAME = "Messages"

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val currentUserUid = Firebase.auth.uid ?: return

        val currentUserReference = Firebase.firestore.document("users/$currentUserUid")
        currentUserReference.get().addOnSuccessListener { value ->
            val currentUser = value?.toObject(User::class.java)
            val messagingToken = currentUser?.messagingToken ?: return@addOnSuccessListener
            Firebase.messaging.unsubscribeFromTopic(messagingToken)
        }
        currentUserReference.update("messagingToken", token).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Token update failed: ", task.exception)
                return@addOnCompleteListener
            }
            Log.d(TAG, "Token updated")
        }

        Firebase.messaging.subscribeToTopic(token)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(MESSAGE_CHANNEL_ID, MESSAGE_CHANNEL_NAME, IMPORTANCE_HIGH).apply {
            description = "Receiving a message from other User"
            enableLights(true)
            lightColor = Color.YELLOW
        }
        notificationManager.createNotificationChannel(channel)
    }
}