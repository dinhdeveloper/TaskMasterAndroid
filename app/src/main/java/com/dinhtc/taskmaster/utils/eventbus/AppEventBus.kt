package com.dinhtc.taskmaster.utils.eventbus

import android.os.Looper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.subjects.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class AppEventBus {
    private var actions: MutableList<EventBusAction.Action> = mutableListOf()
    private var publish: PublishSubject<EventBusAction> = PublishSubject.create<EventBusAction>()
    private var disposables: MutableMap<Any, MutableList<Disposable>> = mutableMapOf()

    fun registerEvent(from: Any, action: EventBusAction.Action, handler: EventBusHandler? = null) {
        if (!actions.contains(action)) actions.add(action)
        publish
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<EventBusAction> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                    var map = disposables[from]
                    if (map == null) map = mutableListOf()
                    disposables[from] = map
                    map.add(d)
                }

                override fun onNext(t: EventBusAction) {
                    val thread = Looper.myLooper()
                    handler?.handleEvent(t)
                }

                override fun onError(e: Throwable) {
                }
            })
    }

    fun unRegisterEvent(from: Any) {
        val map = disposables[from]
        map?.forEach { it.dispose() }
    }

    @JvmOverloads fun publishEvent(action: EventBusAction.Action, obj: Any? = null) {
        if (actions.contains(action)) publish.onNext(EventBusAction(action, obj))
    }

    companion object {
        private var instance: AppEventBus? = null

        @JvmStatic
        fun getInstance(): AppEventBus {
            if (instance == null) instance = AppEventBus()
            return instance!!
        }
    }

    interface EventBusHandler {
        fun handleEvent(result: EventBusAction)
    }
}