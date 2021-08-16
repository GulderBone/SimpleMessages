package com.gulderbone.simple_messages.chat_log

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.base.BaseActivity
import com.gulderbone.simple_messages.databinding.ActivityChatLogBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.extensions.addDisposableTo
import com.gulderbone.simple_messages.messages.LatestMessagesActivity
import com.gulderbone.simple_messages.messages.NewMessageActivity
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.recyclerview_rows.ChatFromItem
import com.gulderbone.simple_messages.recyclerview_rows.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ChatLogActivity : BaseActivity() {
    private lateinit var binding: ActivityChatLogBinding

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val viewModel by lazy { ViewModelProvider(this).get(ChatLogViewModel::class.java) }

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerviewChatLog.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        viewModel.getToUser(toUser)

        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        viewModel.listenForMessages()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ chatMessage ->
                Log.d(TAG, chatMessage.text)
                if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    val currentUser = LatestMessagesActivity.currentUser
                    adapter.add(ChatFromItem(chatMessage.text, currentUser ?: return@subscribe))
                } else {
                    adapter.add(ChatToItem(chatMessage.text, toUser ?: return@subscribe))
                }
                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
            }, {
                Log.e(TAG, "Listening for chat messages failed", it)
            }).addDisposableTo(disposables)
    }

    private fun performSendMessage() {
        val text = binding.edittextChatLog.text.toString()

        val messageSent = viewModel.sendMessage(text)

        if (messageSent) {
            binding.edittextChatLog.text?.clear()
            binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
        }
    }
}

