package com.gulderbone.simple_messages.presentation.chatlog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.databinding.FragmentChatLogBinding
import com.gulderbone.simple_messages.extensions.TAG
import com.gulderbone.simple_messages.extensions.addDisposableTo
import com.gulderbone.simple_messages.main.MainViewModel
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.presentation.latestmessages.LatestMessagesFragment
import com.gulderbone.simple_messages.recyclerview_rows.ChatFromItem
import com.gulderbone.simple_messages.recyclerview_rows.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class ChatLogFragment : BaseFragment<FragmentChatLogBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentChatLogBinding =
        FragmentChatLogBinding::inflate

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val baseViewModel by lazy { ViewModelProvider(requireActivity()).get(MainViewModel::class.java) }

    private val viewModel by lazy { ViewModelProvider(this).get(ChatLogViewModel::class.java) }

    var toUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toUser = arguments?.getParcelable("toUser")
        val username = toUser?.username
        if (username != null) {
            baseViewModel.setActionBarTitle(username)
        }

        listenForMessages()

        with(binding) {
            recyclerviewChatLog.adapter = adapter

            showKeyboard()

            sendButtonChatLog.setOnClickListener {
                sendMessage()
            }
        }
    }

    private fun FragmentChatLogBinding.showKeyboard() {
        edittextChatLog.requestFocus()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(edittextChatLog, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun listenForMessages() {
        viewModel.listenForMessages(toUser)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ chatMessage ->
                Log.d(TAG, chatMessage.text)
                if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    val currentUser = LatestMessagesFragment.currentUser
                    adapter.add(ChatFromItem(chatMessage.text, currentUser ?: return@subscribe))
                } else {
                    adapter.add(ChatToItem(chatMessage.text, toUser ?: return@subscribe))
                }
                binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
            }, {
                Log.e(TAG, "Listening for chat messages failed", it)
            }).addDisposableTo(disposables)
    }

    private fun sendMessage() {
        val text = binding.edittextChatLog.text.toString()

        val messageSent = viewModel.sendMessage(text, toUser)

        if (messageSent) {
            binding.edittextChatLog.text?.clear()
            binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount - 1)
        }
    }
}

