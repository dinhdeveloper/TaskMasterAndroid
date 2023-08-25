package com.dinhtc.taskmaster.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit): Observer<T> {
    val observer = Observer<T> { t -> action(t) }
    liveData.observe(this.viewLifecycleOwner, observer)
    return observer
}

fun <T> Fragment.observeForever(liveData: LiveData<T>, action: (t: T) -> Unit): Observer<T> {
    val observer = Observer<T> { t -> action(t) }
    liveData.observeForever(observer)
    return observer
}

fun <T> Fragment.observe(liveData: LiveData<T>) {
    liveData.observe(this.viewLifecycleOwner, Observer {})
}

fun <T> AppCompatActivity.observe(liveData: LiveData<T>, action: (t: T) -> Unit): Observer<T> {
    val observer = Observer<T> { it?.let { t -> action(t) } }
    liveData.observe(this, observer)
    return observer
}

fun <T> AppCompatActivity.observe(liveData: LiveData<T>) {
    liveData.observe(this, Observer {})
}