package com.gulderbone.simple_messages.extensions

import android.view.View

fun View.gone(): Boolean {
    if (this.visibility == View.VISIBLE) {
        this.visibility = View.GONE
        return true
    }
    return false
}

fun View.invisible(): Boolean {
    if (this.visibility == View.VISIBLE) {
        this.visibility = View.INVISIBLE
        return true
    }
    return false
}

fun View.visible(): Boolean {
    if (this.visibility == View.INVISIBLE || this.visibility == View.GONE) {
        this.visibility = View.VISIBLE
        return true
    }
    return false
}

fun View.visibleOrGone(visible: Boolean) {
    if (visible) {
        this.visible()
    } else {
        this.gone()
    }
}