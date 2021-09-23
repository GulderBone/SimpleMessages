package com.gulderbone.simple_messages.presentation.newchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gulderbone.simple_messages.models.User

class NewChatViewModel : ViewModel() {
    private val users = MutableLiveData<List<User>>()
    private var usersList = listOf<User>()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        val usersReference = Firebase.firestore.collection("users")
        usersReference.addSnapshotListener { value, error ->
            usersList = value?.documents
                ?.mapNotNull { it.toObject(User::class.java) }
                .orEmpty()
            users.postValue(usersList)
        }
    }

    fun getUsers(): LiveData<List<User>> = users
}
