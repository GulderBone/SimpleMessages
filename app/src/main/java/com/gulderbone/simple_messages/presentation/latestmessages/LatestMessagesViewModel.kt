package com.gulderbone.simple_messages.presentation.latestmessages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User

class LatestMessagesViewModel : ViewModel() {
    private val latestMessages = MutableLiveData<HashMap<String, ChatMessage>>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()

    init {
        fetchCurrentUser().addOnSuccessListener {
            subscribeToNewMessageNotifications()
        }
        listenForLatestMessages()
    }

    private fun fetchCurrentUser(): Task<DocumentSnapshot> {
        val uid = FirebaseAuth.getInstance().uid

        val currentUserReference = Firebase.firestore.document("/users/$uid")
        return currentUserReference.get().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Failed to fetch current user: ", task.exception)
                return@addOnCompleteListener
            }
            LatestMessagesFragment.currentUser = task.result?.toObject(User::class.java)
        }
    }

    private fun subscribeToNewMessageNotifications() {
        val token = LatestMessagesFragment.currentUser?.messagingToken
        if (token != null) {
            Firebase.messaging.subscribeToTopic(token)
        } else {
            Log.d(TAG, "Token not generated yet, fetching in progress")
            updateUserToken().addOnSuccessListener { updatedToken ->
                Firebase.messaging.subscribeToTopic(updatedToken)
            }
        }
    }

    private fun updateUserToken(): Task<String> {
        return Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Failed to fetch user token: ", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result

            LatestMessagesFragment.currentUser?.messagingToken = token

            val currentUserId = FirebaseAuth.getInstance().uid
            Firebase.firestore.document("/users/$currentUserId")
                .update("messagingToken", token)
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid

        val latestMessageReference = Firebase.firestore.collection("/users/$fromId/latest_messages")

        latestMessageReference.addSnapshotListener { value, _ ->
            value?.documentChanges?.forEach { documentChange ->
                val chatMessage = documentChange.document.toObject(ChatMessage::class.java)
                val key = chatMessage.toId
                latestMessagesMap[key] = chatMessage
                latestMessages.value = latestMessagesMap
            }
        }
    }


    fun getLatestMessages(): LiveData<HashMap<String, ChatMessage>> = latestMessages
}