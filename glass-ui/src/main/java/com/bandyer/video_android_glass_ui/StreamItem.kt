package com.bandyer.video_android_glass_ui

import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_core_ui.model.Permission
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallMyStreamItemLayoutBinding
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassCallOtherStreamItemLayoutBinding
import com.bandyer.video_android_glass_ui.model.internal.StreamItemData
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * IStreamItem
 */
internal interface IStreamItem {
    /**
     * The streamParticipant data
     */
    val data: StreamItemData

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
    final override val data: StreamItemData
) : AbstractItem<T>(), IStreamItem {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = data.view.hashCode().toLong()

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
        override fun bindView(item: T, payloads: List<Any>) {
            if (payloads.isNotEmpty()) {
                when(val payload = payloads[0]) {
                    is StreamItemData -> {
                        onAudioEnabled(payload.isAudioEnabled)
                        onVideoEnabled(payload.isVideoEnabled)
                    }
                    else -> Unit
                }
                return
            }

            with(item.data) {
                onAudioEnabled(isAudioEnabled)
                onVideoEnabled(isVideoEnabled)
                view?.also { onStreamView(it) }
            }
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: T) = jobs.forEach { it.cancel() }
    }
}

internal class MyStreamItem(
    streamData: StreamItemData,
    val micPermission: StateFlow<Permission>,
    val camPermission: StateFlow<Permission>
) : StreamItem<StreamItem.ViewHolder<MyStreamItem>>(
    streamData
) {

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
     * This method returns the IViewHolder for our item, using the provided View.
     *
     * @return the IViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder<MyStreamItem>(view) {

        private var binding = BandyerGlassCallMyStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: MyStreamItem, payloads: List<Any>) {
            super.bindView(item, payloads)

            jobs += item.micPermission.onEach {
                binding.bandyerMicMutedIcon.isActivated = !it.isAllowed && it.neverAskAgain
            }.launchIn(MainScope())

            jobs += item.camPermission.onEach {
                binding.bandyerCamMutedIcon.isActivated = !it.isAllowed && it.neverAskAgain
            }.launchIn(MainScope())

            binding.bandyerSubtitleLayout.bandyerSubtitle.text = itemView.context.getString(
                R.string.bandyer_glass_you_pattern,
                item.data.userDescription
            )
            binding.bandyerCenteredSubtitle.text = item.data.userDescription
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: MyStreamItem): Unit = with(binding) {
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
            (view.parent as? ViewGroup)?.removeAllViews()
            bandyerVideoWrapper.removeAllViews()
            bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}

internal class OtherStreamItem(streamItemData: StreamItemData) :
    StreamItem<StreamItem.ViewHolder<OtherStreamItem>>(streamItemData) {

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
     * This method returns the IViewHolder for our item, using the provided View.
     *
     * @return the IViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * @suppress
     */
    class ViewHolder(view: View) : StreamItem.ViewHolder<OtherStreamItem>(view) {

        private var binding = BandyerGlassCallOtherStreamItemLayoutBinding.bind(itemView)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: OtherStreamItem, payloads: List<Any>) = with(binding) {
            super.bindView(item, payloads)
            val userDesc = item.data.userDescription
            bandyerSubtitleLayout.bandyerSubtitle.text = userDesc

            val image = item.data.userImage
            if (image != Uri.EMPTY) {
                bandyerAvatar.setImage(image)
                return@with
            }
            bandyerAvatar.setBackground(userDesc.parseToColor())
            bandyerAvatar.setText(userDesc.first().toString())
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: OtherStreamItem): Unit = with(binding) {
            super.unbindView(item)
            unbind()
            bandyerVideoWrapper.removeAllViews()
        }

        override fun onAudioEnabled(value: Boolean) {
            binding.bandyerSubtitleLayout.bandyerSubtitleIcon.visibility =
                if (value) View.GONE else View.VISIBLE
        }

        override fun onVideoEnabled(value: Boolean) = with(binding) {
            bandyerVideoWrapper.visibility = if (value) View.VISIBLE else View.GONE
            bandyerAvatar.visibility = if (value) View.GONE else View.VISIBLE
            bandyerInfoWrapper.gravity = if (value) Gravity.START else Gravity.CENTER
        }

        override fun onStreamView(view: View) = with(binding) {
            (view.parent as? ViewGroup)?.removeAllViews()
            bandyerVideoWrapper.removeAllViews()
            bandyerVideoWrapper.addView(view.apply { id = View.generateViewId() })
        }
    }
}