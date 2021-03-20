package com.example.weather.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class MutableUpdateStateFlow<T>(value: T) : MutableStateFlow<T> {

    private val delegate = MutableStateFlow(UpdateEvent(value))

    override val replayCache: List<T>
        get() = delegate.replayCache.map { it.value }

    override val subscriptionCount: StateFlow<Int>
        get() = delegate.subscriptionCount

    override var value: T
        get() = delegate.value.value
        set(value) {
            delegate.value = UpdateEvent(value)
        }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        delegate.map { it.value }.collect(collector)
    }

    override fun compareAndSet(expect: T, update: T): Boolean =
        delegate.compareAndSet(UpdateEvent(expect), UpdateEvent(update))

    override suspend fun emit(value: T) {
        delegate.emit(UpdateEvent(value))
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        delegate.resetReplayCache()
    }

    override fun tryEmit(value: T): Boolean = delegate.tryEmit(UpdateEvent(value))

    private class UpdateEvent<T>(val value: T)
}