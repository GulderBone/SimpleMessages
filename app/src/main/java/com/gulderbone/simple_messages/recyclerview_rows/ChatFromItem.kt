package com.gulderbone.simple_messages.recyclerview_rows

import android.view.View
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.ChatFromRowBinding
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.viewbinding.BindableItem

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