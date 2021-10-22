package com.bandyer.video_android_glass_ui

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_glass_ui.databinding.BandyerCallParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class StreamItem(val stream: Stream, parentScope: CoroutineScope) :
    AbstractItem<StreamItem.ViewHolder>() {

    private val scope = parentScope + CoroutineName(this.toString() + stream.id)

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = stream.id.hashCode().toLong()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_call_participant_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_participant_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<StreamItem>(view) {

        private var binding = BandyerCallParticipantItemLayoutBinding.bind(itemView)

        private var job: Job? = null

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) {
            job = observeStream(item.stream, item.scope)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            unbind()
            bandyerWrapper.removeAllViews()
            job?.cancel()
        }

        private fun observeStream(stream: Stream, scope: CoroutineScope): Job =
            scope.launch {
                stream.video.collect { video ->
                    video ?: return@collect
                    with(video) {
                        observeEnabled(enabled, this@launch)
                        observeView(view, this@launch)
                    }
                }
            }

        private fun observeEnabled(enabled: Flow<Boolean>, scope: CoroutineScope) =
            enabled.onEach {
                with(binding) {
                    bandyerWrapper.visibility = if (it) View.VISIBLE else View.GONE
                    bandyerCameraEnabled.apply {
                        visibility = if (it) View.GONE else View.VISIBLE
                        text = if (it) null else "Camera disabled"
                    }
                }
            }.launchIn(scope)

        private fun observeView(view: Flow<View?>, scope: CoroutineScope) =
            view.onEach {
                it?.apply {
                    (parent as? ViewGroup)?.removeView(this)
                    binding.bandyerWrapper.addView(this.apply { id = View.generateViewId() })
                }
            }.launchIn(scope)
    }
}