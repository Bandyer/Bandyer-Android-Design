package com.bandyer.video_android_glass_ui

import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerCallParticipantItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class StreamItem(val data: ParticipantStreamInfo, parentScope: CoroutineScope) :
    AbstractItem<StreamItem.ViewHolder>() {

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

        private val jobs: MutableList<Job> = mutableListOf()

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: StreamItem, payloads: List<Any>) = with(binding) {
            bandyerTitle.visibility = View.GONE
            bandyerAvatar.visibility = View.GONE
            bandyerSubtitle.text = item.data.username

            val data = item.data
            val stream = data.stream
            val scope = item.scope

            jobs.add(
                stream.audio
                    .filter { it != null }
                    .flatMapConcat { it!!.enabled }
                    .onEach {
                        bandyerMicIcon.visibility = if (it) View.GONE else View.VISIBLE
                    }.launchIn(scope)
            )

            jobs.add(
                stream.video
                    .filter { it != null }
                    .flatMapConcat { video -> video!!.enabled.combine(video.view) { enabled, view -> Pair(enabled, view) } }
                    .onEach { pair ->
                        val enabled = pair.first
                        val view = pair.second
                        (if (data.isMyStream) bandyerTitle else bandyerAvatar).visibility =
                            if (enabled) View.GONE else View.VISIBLE
                        bandyerVideoWrapper.visibility = if (enabled) View.VISIBLE else View.INVISIBLE
                        bandyerUserWrapper.gravity = if (enabled) Gravity.START else Gravity.CENTER
                        view?.also {
                            (it.parent as? ViewGroup)?.removeView(it)
                            bandyerVideoWrapper.addView(it.apply { id = View.generateViewId() })
                        }
                    }.launchIn(scope)
            )

            if (data.isMyStream) return@with

            data.avatarUrl?.also { bandyerAvatar.setImage(it) } ?: kotlin.run {
                bandyerAvatar.apply {
                    setBackground(data.username.parseToColor())
                    setText(data.username[0].toString())
                }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: StreamItem): Unit = with(binding) {
            unbind()
            bandyerVideoWrapper.removeAllViews()
            jobs.forEach { it.cancel() }
        }
    }
}





