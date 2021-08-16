package com.gulderbone.simple_messages.latestmessages

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.chatlog.ChatLogActivity
import com.gulderbone.simple_messages.databinding.ActivityLatestMessagesBinding
import com.gulderbone.simple_messages.messages.NewMessageActivity
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.recyclerview_rows.LatestMessage
import com.gulderbone.simple_messages.registerlogin.RegisterActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessagesActivity : BaseActivity() {
    private lateinit var binding: ActivityLatestMessagesBinding

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val viewModel by lazy { ViewModelProvider(this).get(LatestMessagesViewModel::class.java) }

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerviewLatestMessages.adapter = adapter
        binding.recyclerviewLatestMessages.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        adapter.setOnItemClickListener { item, view -> onLatestMessageClicks(item, view) }

        listenForLatestMessages()

        viewModel.fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    private fun onLatestMessageClicks(item: Item<*>, view: View) {
        val row = item as LatestMessage
        val intent = Intent(view.context, ChatLogActivity::class.java)
        intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
        startActivity(intent)
    }

    private fun listenForLatestMessages() {
        viewModel.listenForLatestMessages()
        viewModel.getLatestMessages().observe(this) { messagesMap ->
            adapter.clear()
            messagesMap.values.forEach { chatMessage ->
                adapter.add(LatestMessage(chatMessage))
            }
        }
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}