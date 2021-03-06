package com.gulderbone.simple_messages.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String, var messagingToken: String? = null) :
    Parcelable {
    constructor() : this("", "", "")
}