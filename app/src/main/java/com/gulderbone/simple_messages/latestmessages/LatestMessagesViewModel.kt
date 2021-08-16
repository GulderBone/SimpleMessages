package com.gulderbone.simple_messages.latestmessages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User

class LatestMessagesViewModel : ViewModel() {
    val latestMessages = MutableLiveData<HashMap<String, ChatMessage>>()
    val latestMessagesMap = HashMap<String, ChatMessage>()

    fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                val key = snapshot.key ?: return
                latestMessagesMap[key] = chatMessage
                latestMessages.value = latestMessagesMap
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                val key = snapshot.key ?: return
                latestMessagesMap[key] = chatMessage
                latestMessages.value = latestMessagesMap
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                LatestMessagesActivity.currentUser = snapshot.getValue(User::class.java)
                Log.d(TAG, "Current user ${LatestMessagesActivity.currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getLatestMessages(): LiveData<HashMap<String, ChatMessage>> {
        return latestMessages
    }
}