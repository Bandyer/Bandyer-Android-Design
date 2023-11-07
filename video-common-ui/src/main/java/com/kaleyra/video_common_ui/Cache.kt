package com.kaleyra.video_common_ui

interface Cache<K, V> {

    val size: Int

    operator fun set(key: K, value: V)

    operator fun get(key: K): V?

    fun remove(key: K): V?

    fun clear()
}

class PerpetualCache<K, V> : com.kaleyra.video_common_ui.Cache<K, V> {

    private val cache = HashMap<K, V>()

    override val size: Int
        get() = cache.size

    override fun set(key: K, value: V) {
        cache[key] = value
    }

    override fun remove(key: K) = cache.remove(key)

    override fun get(key: K) = cache[key]

    override fun clear() = cache.clear()
}

