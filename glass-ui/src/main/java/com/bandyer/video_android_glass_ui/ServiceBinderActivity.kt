package com.bandyer.video_android_glass_ui

interface ServiceBinderActivity {

    interface ServiceObserver {
        fun onServiceBound()
    }

    /**
     * The observers' list
     */
    val observers: ArrayList<ServiceObserver>

    /**
     * Add a ServiceObserver
     *
     * @param observer ServiceObserver
     */
    fun addBindServiceObserver(observer: ServiceObserver) {
        observers.add(observer)
    }

    /**
     * Remove a ServiceObserver
     *
     * @param observer ServiceObserver
     */
    fun removeBindServiceObserver(observer: ServiceObserver) {
        observers.remove(observer)
    }

    /**
     * Notify all BindServiceObservers
     */
    fun notifyServiceBinding() {
        observers.forEach { it.onServiceBound() }
    }
}