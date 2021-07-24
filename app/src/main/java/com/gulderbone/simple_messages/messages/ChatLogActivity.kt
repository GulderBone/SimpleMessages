package com.gulderbone.simple_messages.messages

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.gulderbone.simple_messages.BaseActivity
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.ActivityChatLogBinding
import com.gulderbone.simple_messages.databinding.ChatFromRowBinding
import com.gulderbone.simple_messages.databinding.ChatToRowBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem

class ChatLogActivity : BaseActivity() {
    private lateinit var binding: ActivityChatLogBinding

    private val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerviewChatLog.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser ?: return))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser ?: return))
                    }
                }

                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun performSendMessage() {
        val text = binding.edittextChatLog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        if (fromId == null || toId == null) return

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

        binding.edittextChatLog.text?.clear()
        binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
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