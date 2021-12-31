package com.gulderbone.simple_messages.presentation.latestmessages

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.base.BaseFragment
import com.gulderbone.simple_messages.databinding.FragmentLatestMessagesBinding
import com.gulderbone.simple_messages.main.MainViewModel
import com.gulderbone.simple_messages.models.User
import com.gulderbone.simple_messages.recyclerview_rows.LatestMessage
import com.gulderbone.simple_messages.utils.CountingIdlingResourceSingleton
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessagesFragment : BaseFragment<FragmentLatestMessagesBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLatestMessagesBinding =
        FragmentLatestMessagesBinding::inflate

    private lateinit var navController: NavController

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val mainViewModel by lazy { ViewModelProvider(requireActivity()).get(MainViewModel::class.java) }

    private val viewModel by lazy { ViewModelProvider(this).get(LatestMessagesViewModel::class.java) }

    companion object {
        var currentUser: User? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        CountingIdlingResourceSingleton.decrement() // TODO Replace with loader in registration and login

        binding.recyclerviewLatestMessages.adapter = adapter
        binding.recyclerviewLatestMessages.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener { item, _ -> onLatestMessageClicks(item) }

        mainViewModel.setLoaderVisibility(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        mainViewModel.setActionBarTitle("Latest Messages")

        listenForLatestMessages()

        super.onResume()
    }

    private fun listenForLatestMessages() {
        viewModel.getLatestMessages().observe(requireActivity()) { messagesMap ->
            adapter.clear()
            messagesMap.values.forEach { chatMessage ->
                adapter.add(LatestMessage(chatMessage))
            }
        }
    }

    private fun onLatestMessageClicks(item: Item<*>) {
        val row = item as LatestMessage
        val bundle = bundleOf("toUser" to row.chatPartnerUser)

        navController.navigate(R.id.action_latestMessagesFragment_to_chatLogFragment, bundle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                navController.navigate(R.id.action_latestMessagesFragment_to_newChatFragment)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(R.id.action_latestMessagesFragment_to_loginFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.nav_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}