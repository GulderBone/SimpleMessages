package com.gulderbone.simple_messages.presentation.chatlog

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User
import io.reactivex.rxjava3.core.Observable
import java.util.*

class ChatLogViewModel : ViewModel() {

    fun listenForMessages(toUser: User?): Observable<ChatMessage> {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val chatLogReference = Firebase.firestore.collection("/users/$fromId/chats/$toId/messages")

        return Observable.create {
            chatLogReference.orderBy("timestamp").addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                value?.documentChanges?.forEach { documentChange ->
                    val chatMessage = documentChange.document.toObject(ChatMessage::class.java)
                    it.onNext(chatMessage)
                }
            }
        }
    }

    fun sendMessage(text: String, toUser: User?): Boolean {
        val fromId = FirebaseAuth.getInstance().uid

        val toId = toUser?.uid

        if (fromId == null || toId == null) return false

        val chatMessage =
            ChatMessage(UUID.randomUUID().toString(), text, fromId, toId, System.currentTimeMillis() / 1000)

        val fromReference2 = Firebase.firestore.collection("users/$fromId/chats/$toId/messages")
        fromReference2.add(chatMessage)

        val fromReference = Firebase.firestore.collection("/users/$toId/chats/$fromId/messages")
        fromReference.add(chatMessage)

        val latestMessageFromReference = Firebase.firestore.document("users/$fromId/latest_messages/$toId")
        latestMessageFromReference.set(chatMessage)

        val latestMessageToReference = Firebase.firestore.document("users/$toId/latest_messages/$fromId")
        latestMessageToReference.set(chatMessage)

        return true
    }
}