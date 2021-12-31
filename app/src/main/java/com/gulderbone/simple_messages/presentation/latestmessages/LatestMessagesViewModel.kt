package com.gulderbone.simple_messages.presentation.latestmessages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User

class LatestMessagesViewModel : ViewModel() {
    private val latestMessages = MutableLiveData<HashMap<String, ChatMessage>>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()

    init {
        fetchCurrentUser()
        listenForLatestMessages()
        updateUserToken().addOnSuccessListener { token ->
            Firebase.messaging.subscribeToTopic(token)
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid

        val currentUserReference = Firebase.firestore.document("/users/$uid")
        currentUserReference.addSnapshotListener { value, _ ->
            LatestMessagesFragment.currentUser = value?.toObject(User::class.java)
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

    private fun updateUserToken(): Task<String> {
        return FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val currentUserId = FirebaseAuth.getInstance().uid
            Firebase.firestore.document("/users/$currentUserId")
                .update("messagingToken", token)
        }
    }

    fun getLatestMessages(): LiveData<HashMap<String, ChatMessage>> = latestMessages
}