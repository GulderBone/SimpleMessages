package com.gulderbone.simple_messages.recyclerview_rows

import android.view.View
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.ChatToRowBinding
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.viewbinding.BindableItem

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