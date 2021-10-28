package com.bandyer.video_android_glass_ui

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerCallParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class StreamItem(val data: ParticipantStreamInfo, parentScope: CoroutineScope) : AbstractItem<StreamItem.ViewHolder>() {

    private val scope = parentScope + CoroutineName(this.toString() + data.stream.id)

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = data.stream.id.hashCode().toLong()

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
        override fun bindView(item: StreamItem, payloads: List<Any>): Unit = with(item) {
            with(binding) {
                bandyerTitle.visibility = View.GONE
                bandyerAvatar.visibility = View.GONE
            }

            job = data.stream.video
                .filter { it != null }
                .flatMapConcat { video ->
                    video!!.enabled.combine(video.view) { enabled, view -> Pair(enabled, view) }
                }.onEach { pair ->
                    val enabled = pair.first
                    val view = pair.second
                    with(binding) {
                        (if (data.isMyStream) bandyerTitle else bandyerAvatar).visibility = if (enabled) View.GONE else View.VISIBLE
                        bandyerWrapper.visibility = if (enabled) View.VISIBLE else View.GONE
                        view?.also {
                            (it.parent as? ViewGroup)?.removeView(it)
                            bandyerWrapper.addView(it.apply { id = View.generateViewId() })
                        }
                    }
                }.launchIn(scope)

            if(item.data.isMyStream) return@with

            data.avatarUrl?.apply { binding.bandyerAvatar.setImage(this) } ?: kotlin.run {
                with(binding.bandyerAvatar) {
                    setBackground(item.data.username.parseToColor())
                    setText(item.data.username[0].toString())
                }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            unbind()
            bandyerWrapper.removeAllViews()
            job?.cancel()
        }
    }
}





