package com.elogictics.taskmaster.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun <T> Fragment.observe(liveData: LiveData<T>, action: (t: T) -> Unit): Observer<T> {
    val observer = Observer<T> { t -> action(t) }
    liveData.observe(this.viewLifecycleOwner, observer)
    return observer
}

fun <T> Fragment.observes(liveData: LiveData<T?>, action: (t: T?) -> Unit): Observer<T?> {
    val observer = Observer<T?> { t -> action(t) }
    liveData.observe(this.viewLifecycleOwner, observer)
    return observer
}

fun <T> Fragment.observe(flow: Flow<T>, action: (t: T) -> Unit): Job {
    return viewLifecycleOwner.lifecycleScope.launch {
        flow.collect { t ->
            action(t)
        }
    }
}



fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
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