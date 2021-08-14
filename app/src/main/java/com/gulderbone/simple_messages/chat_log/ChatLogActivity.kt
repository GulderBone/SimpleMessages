package com.gulderbone.simple_messages.chat_log

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.BaseActivity
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.ActivityChatLogBinding
import com.gulderbone.simple_messages.databinding.ChatFromRowBinding
import com.gulderbone.simple_messages.databinding.ChatToRowBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.messages.LatestMessagesActivity
import com.gulderbone.simple_messages.messages.NewMessageActivity
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
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
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({ chatMessage ->
                Log.d(TAG, chatMessage.text)
                runOnUiThread {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser ?: return@runOnUiThread))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser ?: return@runOnUiThread))
                    }
                    binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
                }
            }, {
                Log.e(TAG, "Listening for chat messages failed", it)
            })
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

class ChatFromItem(val text: String, private val user: User) : BindableItem<ChatFromRowBinding>() {
    override fun bind(viewBinding: ChatFromRowBinding, position: Int) {
        viewBinding.textViewFromRow.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewBinding.imageViewFromRow
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int = R.layout.chat_from_row

    override fun initializeViewBinding(view: View): ChatFromRowBinding = ChatFromRowBinding.bind(view)
}

class ChatToItem(val text: String, private val user: User) : BindableItem<ChatToRowBinding>() {
    override fun bind(viewBinding: ChatToRowBinding, position: Int) {
        viewBinding.textViewToRow.text = text

        val uri = user.profileImageUrl
        val targetImageView = viewBinding.imageViewToRow
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int = R.layout.chat_to_row

    override fun initializeViewBinding(view: View): ChatToRowBinding = ChatToRowBinding.bind(view)
}