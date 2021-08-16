package com.gulderbone.simple_messages.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.chatlog.ChatLogActivity
import com.gulderbone.simple_messages.databinding.ActivityNewMessageBinding
import com.gulderbone.simple_messages.databinding.UserRowNewMessageBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem

class NewMessageActivity : BaseActivity() {
    private lateinit var binding: ActivityNewMessageBinding

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                snapshot.children.forEach {
                    Log.d(TAG, it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()
                }

                binding.recyclerviewNewmessage.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User) : BindableItem<UserRowNewMessageBinding>() {

    override fun bind(viewBinding: UserRowNewMessageBinding, position: Int) {
        viewBinding.usernameTextviewNewMessage.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewBinding.imageviewNewMessage)
    }

    override fun getLayout() = R.layout.user_row_new_message

    override fun initializeViewBinding(view: View): UserRowNewMessageBinding = UserRowNewMessageBinding.bind(view)
}