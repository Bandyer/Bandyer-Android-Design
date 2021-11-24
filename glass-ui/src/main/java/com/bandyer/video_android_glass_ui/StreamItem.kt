package com.bandyer.video_android_glass_ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallMyStreamItemLayoutBinding
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallOtherStreamItemLayoutBinding
import com.bandyer.video_android_glass_ui.model.Input
import com.bandyer.video_android_glass_ui.model.internal.StreamParticipant
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal abstract class StreamItem(val data: StreamParticipant, parentScope: CoroutineScope) : AbstractItem<StreamItem.ViewHolder>() {

    private val scope = parentScope + CoroutineName(this.toString() + data.hashCode())

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = data.hashCode().toLong()

    /**
     * @suppress
     */
    abstract class ViewHolder(view: View) : FastAdapter.ViewHolder<StreamItem>(view) {

        private val jobs = mutableListOf<Job>()

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) = with(item.data.stream) {
            this ?: kotlin.run {
                onAudioEnabled(false)
                onVideoEnabled(false)
                return
            }

            jobs += audio
                .onEach { if (it == null) onAudioEnabled(false) }
                .filter { it != null }
                .flatMapLatest { combine(it!!.state, it.enabled) { s, e -> Pair(s, e) } }
                .onEach { onAudioEnabled(if (it.first is Input.State.Closed) false else it.second) }
                .launchIn(item.scope)

            jobs += video
                .onEach { if (it == null) onVideoEnabled(true) }
                .filter { it != null }
                .flatMapLatest { combine(it!!.state, it.enabled, it.view) { s, e, v -> Triple(s, e, v)} }
                .onEach {
                    onVideoEnabled(if (it.first is Input.State.Closed) false else it.second)
                    it.third?.also { onStreamView(it) }
                }.launchIn(item.scope)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem) {
            jobs.forEach { it.cancel() }
        }

        abstract fun onAudioEnabled(value: Boolean)

        abstract fun onVideoEnabled(value: Boolean)

        abstract fun onStreamView(view: View)

    }
}

internal class MyStreamItem(data: StreamParticipant, parentScope: CoroutineScope) : StreamItem(data, parentScope) {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_call_my_stream_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_my_stream_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder(view) {

        private var binding = BandyerGlassCallMyStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) {
            super.bindView(item, payloads)
            binding.bandyerSubtitleLayout.bandyerSubtitle.text = item.data.participant.username
            binding.bandyerCenteredSubtitle.text = item.data.participant.username
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            super.unbindView(item)
            unbind()
            bandyerVideoWrapper.removeAllViews()
        }

        override fun onAudioEnabled(value: Boolean) = with(binding) {
            val visibility = if (value) View.GONE else View.VISIBLE
            bandyerSubtitleLayout.bandyerSubtitleIcon.visibility = visibility
            bandyerMicMutedIcon.visibility = visibility
        }

        override fun onVideoEnabled(value: Boolean) = with(binding) {
            bandyerVideoWrapper.visibility = if (value) View.VISIBLE else View.GONE
            bandyerCenteredGroup.visibility = if (value) View.GONE else View.VISIBLE
            bandyerSubtitleLayout.root.visibility = if (value) View.VISIBLE else View.GONE
            bandyerInfoWrapper.gravity = if (value) Gravity.START else Gravity.CENTER
        }

        override fun onStreamView(view: View) = with(binding) {
            (view.parent as? ViewGroup)?.removeView(view)
            bandyerVideoWrapper.removeAllViews()
            bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}

internal class OtherStreamItem(data: StreamParticipant, parentScope: CoroutineScope) : StreamItem(data, parentScope) {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_call_other_stream_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_other_stream_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder(view) {

        private var binding = BandyerGlassCallOtherStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) = with(binding) {
            super.bindView(item, payloads)
            val username = item.data.participant.username
            bandyerSubtitleLayout.bandyerSubtitle.text = username
            val avatarUrl = item.data.participant.avatarUrl ?: kotlin.run {
                bandyerAvatar.setBackground(username.parseToColor())
                bandyerAvatar.setText(username[0].toString())
                return@with
            }
            bandyerAvatar.setImage(avatarUrl)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            super.unbindView(item)
            unbind()
            bandyerVideoWrapper.removeAllViews()
        }

        override fun onAudioEnabled(value: Boolean) {
            binding.bandyerSubtitleLayout.bandyerSubtitleIcon.visibility = if (value) View.GONE else View.VISIBLE
        }

        override fun onVideoEnabled(value: Boolean) = with(binding) {
            bandyerVideoWrapper.visibility = if (value) View.VISIBLE else View.GONE
            bandyerAvatar.visibility = if (value) View.GONE else View.VISIBLE
            bandyerInfoWrapper.gravity = if (value) Gravity.START else Gravity.CENTER
        }

        override fun onStreamView(view: View) = with(binding) {
            (view.parent as? ViewGroup)?.removeView(view)
            bandyerVideoWrapper.removeAllViews()
            bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}