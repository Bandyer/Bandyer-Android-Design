package com.bandyer.video_android_glass_ui

enum class UiEvent {
    LAUNCH,
    DESTROY
}

interface UiEventObserver {
    fun onEvent(event: UiEvent)
}

object UiEventNotifier {

    private val observers: MutableList<UiEventObserver> = mutableListOf()

    fun addObserver(observer: UiEventObserver) { observers.add(observer) }

    fun removeObserver(observer: UiEventObserver) { observers.remove(observer) }

    internal fun notify(event: UiEvent) { observers.forEach { it.onEvent(event) } }
}

