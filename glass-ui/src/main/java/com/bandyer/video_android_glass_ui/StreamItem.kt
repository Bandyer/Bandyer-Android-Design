package com.bandyer.video_android_glass_ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallParticipantItemLayoutBinding
import com.bandyer.video_android_glass_ui.model.Input
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class StreamItem(val data: StreamParticipant, parentScope: CoroutineScope) : AbstractItem<StreamItem.ViewHolder>() {

    private val scope = parentScope + CoroutineName(this.toString() + data.hashCode())

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = data.hashCode().toLong()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_call_participant_item_layout

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

        private var binding = BandyerGlassCallParticipantItemLayoutBinding.bind(itemView)

        private val jobs = mutableListOf<Job>()

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) = with(binding) {
            val data = item.data
            val stream = data.stream

            (if (!data.isMyStream) bandyerTitle else bandyerAvatar).visibility = View.GONE
            bandyerSubtitle.text = item.data.participant.username

            if (!data.isMyStream) {
                data.participant.avatarUrl?.also { bandyerAvatar.setImage(it) } ?: kotlin.run {
                    bandyerAvatar.apply {
                        val username = data.participant.username
                        setBackground(username.parseToColor())
                        setText(username[0].toString())
                    }
                }
            }

            if(stream == null) {
                showMicMuted(true)
                showTitleAvatar(true, data.isMyStream)
                return@with
            }

            jobs.add(stream.audio
                .onEach { if(it == null) showMicMuted(true) }
                .filter { it != null }
                .flatMapLatest { combine(it!!.state, it.enabled) { s, e -> Pair(s, e) } }
                .onEach {
                    if(it.first is Input.State.Closed) showMicMuted(true) else showMicMuted(!it.second)
                }.launchIn(item.scope))

            jobs.add(stream.video
                .onEach { if(it == null) showTitleAvatar(true, data.isMyStream) }
                .filter { it != null }
                .flatMapLatest { combine(it!!.state, it.enabled, it.view) { s, e, v -> Triple(s, e, v) } }
                .onEach {
                    if(it.first is Input.State.Closed) showTitleAvatar(true, data.isMyStream) else showTitleAvatar(!it.second, data.isMyStream)
                    it.third?.also { view ->
                        (view.parent as? ViewGroup)?.removeView(view)
                        bandyerVideoWrapper.removeAllViews()
                        bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
                    }
                }.launchIn(item.scope))
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            unbind()
            bandyerVideoWrapper.removeAllViews()
            jobs.forEach { it.cancel() }
        }

        private fun showMicMuted(value: Boolean) {
            binding.bandyerMicMutedIcon.visibility = if(value) View.VISIBLE else View.GONE
        }

        private fun showTitleAvatar(value: Boolean, isMyStream: Boolean) = with(binding) {
            (if (isMyStream) bandyerTitle else bandyerAvatar).visibility = if (value) View.VISIBLE else View.GONE
            bandyerVideoWrapper.visibility = if (value) View.GONE else View.VISIBLE
            bandyerUserWrapper.gravity = if (value) Gravity.CENTER else Gravity.START
        }
    }
}





