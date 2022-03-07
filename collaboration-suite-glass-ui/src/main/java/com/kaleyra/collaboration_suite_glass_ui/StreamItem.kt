/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui

import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_core_ui.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassCallMyStreamItemLayoutBinding
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassCallOtherStreamItemLayoutBinding
import com.kaleyra.collaboration_suite_core_ui.model.Permission
import com.kaleyra.collaboration_suite_glass_ui.model.internal.StreamParticipant
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

/**
 * IStreamItem
 */
internal interface IStreamItem {
    /**
     * The streamParticipant data
     */
    val streamParticipant: StreamParticipant

    /**
     * The flows' coroutine scope
     */
    val scope: CoroutineScope

    /**
     * IViewHolder
     */
    interface IViewHolder {
        /**
         * The flows' jobs
         */
        val jobs: ArrayList<Job>

        /**
         * Called when the audio is enabled/disabled
         *
         * @param value True if the audio is enabled, false otherwise
         */
        fun onAudioEnabled(value: Boolean)

        /**
         * Called when the video is enabled/disabled
         *
         * @param value True if the video is enabled, false otherwise
         */
        fun onVideoEnabled(value: Boolean)

        /**
         * Called when the stream view is changed
         *
         * @param view The stream view
         */
        fun onStreamView(view: View)
    }
}

/**
 * StreamItem
 *
 * @constructor
 */
internal abstract class StreamItem<T : RecyclerView.ViewHolder>(
    final override val streamParticipant: StreamParticipant,
    parentScope: CoroutineScope
) : AbstractItem<T>(), IStreamItem {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = streamParticipant.hashCode().toLong()

    /**
     * The coroutine scope where the flows will be observed
     */
    override val scope: CoroutineScope =
        parentScope + CoroutineName(this.toString() + streamParticipant.hashCode())

    /**
     * @suppress
     */
    internal abstract class ViewHolder<T : StreamItem<*>>(view: View) :
        FastAdapter.ViewHolder<T>(view), IStreamItem.IViewHolder {

        /**
         * The jobs launched in the ViewHolder
         */
        override val jobs: ArrayList<Job> = arrayListOf()

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: T, payloads: List<Any>) = with(item.streamParticipant.stream) {
            jobs += audio
                .onEach { if (it == null) onAudioEnabled(false) }
                .filter { it != null }
                .flatMapLatest { combine(it!!.state, it.enabled) { s, e -> Pair(s, e) } }
                .onEach { onAudioEnabled(if (it.first !is Input.State.Active) false else it.second) }
                .launchIn(item.scope)

            jobs += video
                .onEach { if (it == null) onVideoEnabled(false) }
                .filter { it != null }
                .flatMapLatest {
                    combine(it!!.state, it.enabled, it.view) { s, e, v ->
                        Triple(
                            s,
                            e,
                            v
                        )
                    }
                }
                .onEach {
                    onVideoEnabled(if (it.first !is Input.State.Active) false else it.second)
                    it.third?.also { onStreamView(it) }
                }.launchIn(item.scope)
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: T) = jobs.forEach { it.cancel() }
    }
}

internal class MyStreamItem(
    streamParticipant: StreamParticipant,
    parentScope: CoroutineScope,
    val micPermission: StateFlow<Permission>,
    val camPermission: StateFlow<Permission>
) : StreamItem<StreamItem.ViewHolder<MyStreamItem>>(
    streamParticipant,
    parentScope
) {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_call_my_stream_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_my_stream_item

    /**
     * This method returns the IViewHolder for our item, using the provided View.
     *
     * @return the IViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder<MyStreamItem>(view) {

        private var binding = KaleyraGlassCallMyStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: MyStreamItem, payloads: List<Any>) {
            super.bindView(item, payloads)

            jobs += item.micPermission.onEach {
                binding.kaleyraMicMutedIcon.isActivated = !it.isAllowed && it.neverAskAgain
            }.launchIn(item.scope)

            jobs += item.camPermission.onEach {
                binding.kaleyraCamMutedIcon.isActivated = !it.isAllowed && it.neverAskAgain
            }.launchIn(item.scope)

            binding.kaleyraSubtitleLayout.kaleyraSubtitle.text = itemView.context.getString(
                R.string.kaleyra_glass_you_pattern,
                item.streamParticipant.userDescription
            )
            binding.kaleyraCenteredSubtitle.text = item.streamParticipant.userDescription
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: MyStreamItem): Unit = with(binding) {
            super.unbindView(item)
            unbind()
            kaleyraVideoWrapper.removeAllViews()
        }

        override fun onAudioEnabled(value: Boolean) = with(binding) {
            val visibility = if (value) View.GONE else View.VISIBLE
            kaleyraSubtitleLayout.kaleyraSubtitleIcon.visibility = visibility
            kaleyraMicMutedIcon.visibility = visibility
        }

        override fun onVideoEnabled(value: Boolean) = with(binding) {
            kaleyraVideoWrapper.visibility = if (value) View.VISIBLE else View.GONE
            kaleyraCenteredGroup.visibility = if (value) View.GONE else View.VISIBLE
            kaleyraSubtitleLayout.root.visibility = if (value) View.VISIBLE else View.GONE
            kaleyraInfoWrapper.gravity = if (value) Gravity.START else Gravity.CENTER
        }

        override fun onStreamView(view: View) = with(binding) {
            (view.parent as? ViewGroup)?.removeAllViews()
            kaleyraVideoWrapper.removeAllViews()
            kaleyraVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}

internal class OtherStreamItem(streamParticipant: StreamParticipant, parentScope: CoroutineScope) :
    StreamItem<StreamItem.ViewHolder<OtherStreamItem>>(streamParticipant, parentScope) {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_call_other_stream_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_call_other_stream_item

    /**
     * This method returns the IViewHolder for our item, using the provided View.
     *
     * @return the IViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder<OtherStreamItem>(view) {

        private var binding = KaleyraGlassCallOtherStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: OtherStreamItem, payloads: List<Any>) = with(binding) {
            super.bindView(item, payloads)
            val userDesc = item.streamParticipant.userDescription
            kaleyraSubtitleLayout.kaleyraSubtitle.text = userDesc

            val image = item.streamParticipant.userImage
            if (image != Uri.EMPTY) {
                kaleyraAvatar.setImage(image)
                return@with
            }
            kaleyraAvatar.setBackground(userDesc.parseToColor())
            kaleyraAvatar.setText(userDesc.first().toString())
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: OtherStreamItem): Unit = with(binding) {
            super.unbindView(item)
            unbind()
            kaleyraVideoWrapper.removeAllViews()
        }

        override fun onAudioEnabled(value: Boolean) {
            binding.kaleyraSubtitleLayout.kaleyraSubtitleIcon.visibility =
                if (value) View.GONE else View.VISIBLE
        }

        override fun onVideoEnabled(value: Boolean) = with(binding) {
            kaleyraVideoWrapper.visibility = if (value) View.VISIBLE else View.GONE
            kaleyraAvatar.visibility = if (value) View.GONE else View.VISIBLE
            kaleyraInfoWrapper.gravity = if (value) Gravity.START else Gravity.CENTER
        }

        override fun onStreamView(view: View) = with(binding) {
            (view.parent as? ViewGroup)?.removeAllViews()
            kaleyraVideoWrapper.removeAllViews()
            kaleyraVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}