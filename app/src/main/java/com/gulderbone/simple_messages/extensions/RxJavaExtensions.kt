package com.gulderbone.simple_messages.extensions

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

fun Disposable.addDisposableTo(compositeDisposable: CompositeDisposable) =
    compositeDisposable.add(this)