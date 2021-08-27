package com.gulderbone.simple_messages.recyclerview_rows

import android.view.View
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.databinding.UserRowNewMessageBinding
import com.gulderbone.simple_messages.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.viewbinding.BindableItem

class UserItem(val user: User) : BindableItem<UserRowNewMessageBinding>() {

    override fun bind(viewBinding: UserRowNewMessageBinding, position: Int) {
        viewBinding.usernameTextviewNewMessage.text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewBinding.imageviewNewMessage)
    }

    override fun getLayout() = R.layout.user_row_new_message

    override fun initializeViewBinding(view: View): UserRowNewMessageBinding = UserRowNewMessageBinding.bind(view)
}