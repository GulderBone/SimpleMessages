package com.gulderbone.simple_messages.presentation.latestmessages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User

class LatestMessagesViewModel : ViewModel() {
    private val latestMessages = MutableLiveData<HashMap<String, ChatMessage>>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()

    init {
        fetchCurrentUser()
        listenForLatestMessages()
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid

        val currentUserReference = Firebase.firestore.document("/users/$uid")
        currentUserReference.addSnapshotListener { value, error ->
            LatestMessagesActivity.currentUser = value?.toObject(User::class.java)
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid

        val latestMessageReference = Firebase.firestore.collection("/users/$fromId/latest_messages")

        latestMessageReference.addSnapshotListener { value, error ->
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