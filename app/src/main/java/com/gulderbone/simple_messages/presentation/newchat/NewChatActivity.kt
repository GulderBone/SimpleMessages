package com.gulderbone.simple_messages.presentation.newchat

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.databinding.ActivityNewChatBinding
import com.gulderbone.simple_messages.presentation.chatlog.ChatLogActivity
import com.gulderbone.simple_messages.recyclerview_rows.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class NewChatActivity : BaseActivity() {
    private lateinit var binding: ActivityNewChatBinding

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val viewModel by lazy { ViewModelProvider(this).get(NewChatViewModel::class.java) }

    companion object {
        const val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select User"

        displayUsers()

        setOnItemClickListener()
    }

    private fun displayUsers() {
        viewModel.getUsers().observe(this) { users ->
            users.forEach { user ->
                adapter.add(UserItem(user))
            }
        }

        binding.recyclerviewNewmessage.adapter = adapter
    }

    private fun setOnItemClickListener() {
        adapter.setOnItemClickListener { item, view ->
            val userItem = item as UserItem
            val intent = Intent(view.context, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, userItem.user)
            startActivity(intent)

            finish()
        }
    }
}
