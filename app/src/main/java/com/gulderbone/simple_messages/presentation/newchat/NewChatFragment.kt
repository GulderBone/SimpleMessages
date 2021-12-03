package com.gulderbone.simple_messages.presentation.newchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.databinding.FragmentNewChatBinding
import com.gulderbone.simple_messages.main.MainViewModel
import com.gulderbone.simple_messages.recyclerview_rows.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class NewChatFragment : BaseFragment<FragmentNewChatBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNewChatBinding =
        FragmentNewChatBinding::inflate

    private lateinit var navController: NavController

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val mainViewModel by lazy { ViewModelProvider(requireActivity()).get(MainViewModel::class.java) }

    private val viewModel by lazy { ViewModelProvider(this).get(NewChatViewModel::class.java) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        mainViewModel.setActionBarTitle("Select User")

        displayUsers()

        setOnItemClickListener()
    }

    private fun displayUsers() {
        viewModel.getUsers().observe(requireActivity()) { users ->
            users.forEach { user ->
                adapter.add(UserItem(user))
            }
        }

        binding.recyclerviewNewmessage.adapter = adapter
    }

    private fun setOnItemClickListener() {
        adapter.setOnItemClickListener { item, _ ->
            val userItem = item as UserItem
            val bundle = bundleOf("toUser" to userItem.user)

            navController.navigate(R.id.action_newChatFragment_to_chatLogFragment, bundle)
        }
    }
}
