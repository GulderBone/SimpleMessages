package com.gulderbone.simple_messages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val actionBarTitle = MutableLiveData<String>()
    private val loaderVisible = MutableLiveData(false)

    fun setActionBarTitle(title: String) {
        actionBarTitle.value = title
    }

    fun actionBarTitle(): LiveData<String> {
        return actionBarTitle
    }

    fun setLoaderVisibility(visible: Boolean) {
        loaderVisible.value = visible
    }

    fun loaderVisibility(): LiveData<Boolean> {
        return loaderVisible
    }
}