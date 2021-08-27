package com.gulderbone.simple_messages.presentation.newchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gulderbone.simple_messages.models.User

class NewChatViewModel : ViewModel() {
    private val users = MutableLiveData<List<User>>()
    private var usersList = listOf<User>()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                users.postValue(usersList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun getUsers(): LiveData<List<User>> = users
}
