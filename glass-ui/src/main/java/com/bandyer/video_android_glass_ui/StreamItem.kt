package com.bandyer.video_android_glass_ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallParticipantItemLayoutBinding
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

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) = with(binding) {
            bandyerTitle.visibility = View.GONE
            bandyerAvatar.visibility = View.GONE
            bandyerMicIcon.visibility = View.GONE
            bandyerSubtitle.text = item.data.participant.username

            val data = item.data
            val stream = data.stream

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
                (if (data.isMyStream) bandyerTitle else bandyerAvatar).visibility = View.VISIBLE
                bandyerVideoWrapper.visibility = View.GONE
                bandyerUserWrapper.gravity = Gravity.CENTER
                bandyerMicIcon.visibility = View.VISIBLE
                return@with
            }

            stream.audio
                .filter { it != null }
                .flatMapConcat { it!!.enabled }
                .onEach { bandyerMicIcon.visibility = if (it) View.GONE else View.VISIBLE }
                .launchIn(item.scope)

            stream.video
                .filter { it != null }
                .flatMapConcat { video -> video!!.enabled.combine(video.view) { enabled, view -> Pair(enabled, view) } }
                .onEach {
                    (if (data.isMyStream) bandyerTitle else bandyerAvatar).visibility = if (it.first) View.GONE else View.VISIBLE
                    bandyerVideoWrapper.visibility = if (it.first) View.VISIBLE else View.GONE
                    bandyerUserWrapper.gravity = if (it.first) Gravity.START else Gravity.CENTER
                    it.second?.also { view ->
                        (view.parent as? ViewGroup)?.removeView(view)
                        bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
                    }
                }.launchIn(item.scope)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            unbind()
            bandyerVideoWrapper.removeAllViews()
            item.scope.coroutineContext.cancelChildren()
        }
    }
}





