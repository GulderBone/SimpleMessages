package com.gulderbone.simple_messages.recyclerview_rows

import android.view.View
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.LatestMessageRowBinding
import com.gulderbone.simple_messages.models.ChatMessage
import com.gulderbone.simple_messages.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.viewbinding.BindableItem

class LatestMessage(private val chatMessage: ChatMessage) : BindableItem<LatestMessageRowBinding>() {
    var chatPartnerUser: User? = null

    override fun bind(viewBinding: LatestMessageRowBinding, position: Int) {
        viewBinding.messageTextviewLatestMessage.text = chatMessage.text

        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else {
            chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewBinding.usernameTextviewLatestMessage.text = chatPartnerUser?.username

                val targetImageView = viewBinding.imageViewLatestMessage
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int = R.layout.latest_message_row

    override fun initializeViewBinding(view: View): LatestMessageRowBinding = LatestMessageRowBinding.bind(view)
}