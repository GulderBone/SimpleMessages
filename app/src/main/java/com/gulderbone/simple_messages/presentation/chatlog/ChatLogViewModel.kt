package com.gulderbone.simple_messages.presentation.chatlog

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User
import io.reactivex.rxjava3.core.Observable

class ChatLogViewModel : ViewModel() {

    fun listenForMessages(toUser: User?): Observable<ChatMessage> {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val chatLogReference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        return Observable.create {
            chatLogReference.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                    it.onNext(chatMessage)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun sendMessage(text: String, toUser: User?): Boolean {
        val fromId = FirebaseAuth.getInstance().uid

        val toId = toUser?.uid

        if (fromId == null || toId == null) return false

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Save our chat message: ${reference.key}")
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

        return true
    }
}