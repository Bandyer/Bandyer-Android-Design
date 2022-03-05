/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.AudioRouteState
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.AudioRouteIconView
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.AudioRouteTextView
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAudioRouteItemStyle
import com.kaleyra.collaboration_suite_phone_ui.extensions.setTextAppearance

/**
 * Audio route
 *
 * @property ctx Context
 * @property identifier id of the item
 * @property name name of the item
 * @property isActive true if is active, false otherwise
 * @constructor
 *
 * @param viewId id of the view
 * @param viewLayoutRes layout resource to inflate
 * @param viewStyle style of the layout
 */
sealed class AudioRoute(val ctx: Context?, val identifier: String, val name: String? = null, var isActive: Boolean, @IdRes viewId: Int, @LayoutRes viewLayoutRes: Int = 0, @StyleRes viewStyle: Int = 0) : ActionItem(viewId, viewLayoutRes, viewStyle) {

    /**
     * Icon of the audio route
     */
    protected var icon: AudioRouteIconView? = null

    /**
     * Title of the audio route
     */
    protected var title: AudioRouteTextView? = null

    /**
     * Subtitle of the audio route
     */
    protected var subtitle: AudioRouteTextView? = null

    /**
     * @suppress
     */
    companion object {

        /**
         * Get an AudioRoute based on the device class, identifier and name
         * @param deviceType Class<out AudioRoute>
         * @param identifier String
         * @param deviceName String
         * @return AudioRoute
         */
        fun getAudioRoute(ctx: Context, deviceType: Class<out AudioRoute>, identifier: String, deviceName: String, isActive: Boolean, bluetoothConnectionStatus: AudioRouteState.BLUETOOTH? = null): AudioRoute {
            return when (deviceType) {
                LOUDSPEAKER::class.java -> LOUDSPEAKER(ctx, identifier, deviceName, isActive = isActive)
                BLUETOOTH::class.java -> BLUETOOTH(ctx, identifier, deviceName, batteryLevel = null, bluetoothConnectionStatus = bluetoothConnectionStatus
                        ?: AudioRouteState.BLUETOOTH.PLAYING_AUDIO())
                EARPIECE::class.java -> EARPIECE(ctx, identifier, deviceName, isActive = isActive)
                WIRED_HEADSET::class.java -> WIRED_HEADSET(ctx, identifier, deviceName, isActive)
                MUTED::class.java -> MUTED(ctx, identifier, deviceName, isActive)
                else -> LOUDSPEAKER(ctx, identifier, deviceName, isActive = isActive)
            }
        }
    }

    /**
     * Bluetooth AudioRoute Item
     *
     * @property batteryLevel current batteryLevel
     * @property bluetoothConnectionStatus status of the device
     * @constructor
     */
    open class BLUETOOTH(ctx: Context? = null,
                         identifier: String,
                         name: String? = null,
                         var batteryLevel: Int? = null,
                         var bluetoothConnectionStatus: AudioRouteState.BLUETOOTH)
        : AudioRoute(ctx, identifier, name, bluetoothConnectionStatus is AudioRouteState.BLUETOOTH.PLAYING_AUDIO, identifier.hashCode(), R.layout.kaleyra_call_audioroute_item, ctx?.getAudioRouteItemStyle(R.styleable.KaleyraSDKDesign_BottomSheet_AudioRoute_kaleyra_bluetoothItemStyle)
            ?: 0) {

        override fun onReady() {
            super.onReady()

            setAudioRouteItemState(bluetoothConnectionStatus)

            val resources = ctx?.resources ?: return

            val btStatus: String = when {
                bluetoothConnectionStatus is AudioRouteState.BLUETOOTH.DISCONNECTED -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_disconnected)
                bluetoothConnectionStatus is AudioRouteState.BLUETOOTH.FAILED -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_failed)
                bluetoothConnectionStatus is AudioRouteState.BLUETOOTH.AVAILABLE -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_available)
                bluetoothConnectionStatus.isConnectedOrPlaying() -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_connected)
                bluetoothConnectionStatus is AudioRouteState.BLUETOOTH.DEACTIVATING -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_deactivating)
                else -> ""
            }

            val battery: String = batteryLevel?.let { resources.getString(R.string.kaleyra_bluetooth_battery_info, resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_battery_level), it) }
                    ?: ""

            val connectingStatus: String = when {
                bluetoothConnectionStatus.isConnecting() && (btStatus.isNotBlank() || battery.isNotBlank()) -> resources.getString(R.string.kaleyra_bluetooth_connecting_status_info, resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating))
                bluetoothConnectionStatus.isConnecting() -> resources.getString(R.string.kaleyra_call_action_audio_route_bluetooth_activating)
                else -> ""
            }

            val bluetoothInfo = resources.getString(R.string.kaleyra_bluetooth_info, btStatus, battery, connectingStatus)

            if (bluetoothInfo.isNotBlank()) {
                subtitle?.text = bluetoothInfo
                subtitle?.visibility = View.VISIBLE
            } else subtitle?.visibility = View.GONE
        }
    }

    /**
     * Loudspeaker AudioRoute Item
     * @constructor
     */
    open class LOUDSPEAKER(ctx: Context? = null, identifier: String, name: String? = null, isActive: Boolean = false) : AudioRoute(ctx, identifier, name, isActive, R.id.kaleyra_id_loudspeaker, R.layout.kaleyra_call_audioroute_item, ctx?.getAudioRouteItemStyle(R.styleable.KaleyraSDKDesign_BottomSheet_AudioRoute_kaleyra_loudspeakerItemStyle)
            ?: 0)

    /**
     * Earpiece AudioRoute Item
     * @constructor
     */
    open class EARPIECE(ctx: Context? = null, identifier: String, name: String? = null, isActive: Boolean = false) : AudioRoute(ctx, identifier, name, isActive, R.id.kaleyra_id_earpiece, R.layout.kaleyra_call_audioroute_item, ctx?.getAudioRouteItemStyle(R.styleable.KaleyraSDKDesign_BottomSheet_AudioRoute_kaleyra_earpieceItemStyle)
            ?: 0)

    /**
     * Wired headset AudioRoute Item
     * @constructor
     */
    open class WIRED_HEADSET(ctx: Context? = null, identifier: String, name: String? = null, isActive: Boolean = false) : AudioRoute(ctx, identifier, name, isActive, R.id.kaleyra_id_wiredheadset, R.layout.kaleyra_call_audioroute_item, ctx?.getAudioRouteItemStyle(R.styleable.KaleyraSDKDesign_BottomSheet_AudioRoute_kaleyra_wiredHeadsetItemStyle)
            ?: 0)

    /**
     * Muted AudioRoute Item
     * @constructor
     */
    open class MUTED(ctx: Context? = null, identifier: String, name: String? = null, isActive: Boolean = false) : AudioRoute(ctx, identifier, name, isActive, R.id.kaleyra_id_muted, R.layout.kaleyra_call_audioroute_item, ctx?.getAudioRouteItemStyle(R.styleable.KaleyraSDKDesign_BottomSheet_AudioRoute_kaleyra_mutedItemStyle)
            ?: 0)

    /**
     * Called when the layout has been inflated
     */
    override fun onReady() {
        val deviceName = name ?: return

        icon = itemView?.findViewById(R.id.kaleyra_audio_item_icon)
        title = itemView?.findViewById(R.id.kaleyra_audio_item_title)
        subtitle = itemView?.findViewById(R.id.kaleyra_audio_item_subtitle)
        title?.text = deviceName

        if (isActive) {
            setAudioRouteItemState(AudioRouteState.BLUETOOTH.PLAYING_AUDIO())
            ctx?.setTextAppearance(title, R.style.KaleyraSDKDesign_TextView_Title_AudioRoute_TextAppearance_Active)
        } else {
            setAudioRouteItemState(AudioRouteState.DEFAULT())
            ctx?.setTextAppearance(title, R.style.KaleyraSDKDesign_TextView_Title_AudioRoute_TextAppearance)
        }
    }

    /**
     * @suppress
     */
    protected fun setAudioRouteItemState(state: AudioRouteState) {
        icon?.state = state
        title?.state = state
        subtitle?.state = state
    }

    /**
     * Has changed
     *
     * @param other
     * @return true if has changed, false otherwise
     */
    fun hasChanged(other: Any?): Boolean {
        other ?: return false
        if (this is BLUETOOTH && other is BLUETOOTH) {
            return this.identifier == other.identifier &&
                    (this.name != other.name ||
                            this.batteryLevel != other.batteryLevel ||
                            this.bluetoothConnectionStatus != other.bluetoothConnectionStatus)
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as AudioRoute

        if (identifier != other.identifier) return false
        if (name != other.name) return false
        if (isActive != other.isActive) return false
        return !hasChanged(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + identifier.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + isActive.hashCode()
        return result
    }
}