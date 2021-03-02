/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.call.bottom_sheet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheet
import com.bandyer.sdk_design.bottom_sheet.BandyerSelectableBottomSheet
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.items.AdapterActionItem
import com.bandyer.sdk_design.bottom_sheet.view.AudioRouteState
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.buttons.BandyerLineButton.State
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.select.SelectExtension

/**
 * AudioRoute BottomSheet to display the available audioRoutes of the device
 * @param context Context
 * @param onAudioRoutesRequest used to request available audioRoutes
 * @constructor
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
class AudioRouteBottomSheet<T : ActionItem>(val context: AppCompatActivity,
                                            audioRouteItems: List<AudioRoute>?,
                                            initial_selection: Int = -1,
                                            bottomSheetStyle: Int,
                                            var onAudioRoutesRequest: OnAudioRouteBottomSheetListener?) : BandyerSelectableBottomSheet<T>(
        context,
        initial_selection,
        audioRouteItems as List<T>? ?: listOf<T>(),
        0, 0,
        BottomSheetLayoutType.LIST,
        bottomSheetStyle) {

    /**
     * Returns the current selected Audio Route
     */
    var mCurrentAudioRoute: AudioRoute? = null

    init {
        animationEnabled = true
        recyclerView?.itemAnimator = null
    }

    override fun show() {
        super.show()
        onAudioRoutesRequest?.onAudioRoutesRequested()?.let { setItems(it) }
        selectItem(mCurrentAudioRoute)
        bottomSheetBehaviour!!.skipCollapsed = true
        bottomSheetBehaviour!!.isHideable = true
        bottomSheetBehaviour!!.skipAnchor = true
        bottomSheetLayoutContent.backgroundView?.alpha = 1f
        expand()
    }

    override fun saveInstanceState(saveInstanceState: Bundle?): Bundle? {
        val currentAudioRoute = mCurrentAudioRoute ?: return onSaveInstanceState(saveInstanceState, "audio")
        saveInstanceState?.putString("currentAudioRoute", currentAudioRoute.javaClass.name.toString())
        saveInstanceState?.putString("currentAudioRouteDeviceName", currentAudioRoute.name)
        saveInstanceState?.putString("currentAudioRouteIdentifier", currentAudioRoute.identifier)
        saveInstanceState?.putBoolean("isActive", currentAudioRoute.isActive)
        (currentAudioRoute as? AudioRoute.BLUETOOTH)?.let {
            saveInstanceState?.putSerializable("currentBluetoothConnectionStatus", it.bluetoothConnectionStatus)
        }
        return onSaveInstanceState(saveInstanceState, "audio")
    }

    override fun restoreInstanceState(bundle: Bundle?) {
        onRestoreInstanceState(bundle, "audio")
        val currentAudioRoute = bundle?.getString("currentAudioRoute") ?: return
        val currentAudioRouteDeviceName = bundle.getString("currentAudioRouteDeviceName") ?: ""
        val currentAudioRouteIdentifier = bundle.getString("currentAudioRouteIdentifier") ?: ""
        val isActive = bundle.getBoolean("isActive")

        val currentBluetoothConnectionStatus: AudioRouteState.BLUETOOTH? = bundle.getSerializable("currentBluetoothConnectionStatus") as? AudioRouteState.BLUETOOTH

        mCurrentAudioRoute = AudioRoute.getAudioRoute(context, Class.forName(currentAudioRoute) as Class<AudioRoute>, currentAudioRouteIdentifier, currentAudioRouteDeviceName, isActive, currentBluetoothConnectionStatus)
    }

    /**
     * Select another audioRoute
     * @param audioRoute AudioRoute? to select
     */
    fun selectAudioRoute(audioRoute: AudioRoute?) {
        if (audioRoute == null || mCurrentAudioRoute == audioRoute) return

        mCurrentAudioRoute = audioRoute
        selectItem(audioRoute)
    }

    /**
     * Select the audio router item provided
     * @param actionItem ActionItem to select
     */
    override fun selectItem(actionItem: ActionItem?) {
        if (actionItem == null || (actionItem as? AudioRoute) == null)
            return

        currentItemSelected = AdapterActionItem(actionItem)

        fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.deselect()

        val position = fastAdapter.adapterItems.indexOfFirst {
            (it.item as AudioRoute).identifier == actionItem.identifier
        }.takeIf { it != -1 } ?: fastAdapter.adapterItems.indexOfFirst {
            it.item is AudioRoute.MUTED
        }

        fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.select(position)
    }

    override fun setItems(items: List<ActionItem>) {
        val diffResult = FastAdapterDiffUtil.calculateDiff(fastAdapter, items.map { AdapterActionItem(it) })
        FastAdapterDiffUtil.set(fastAdapter, diffResult)

        mCurrentAudioRoute?.let { selectItem(it) }

        if (state == BottomSheetBehavior.STATE_EXPANDED || state == BottomSheetBehavior.STATE_COLLAPSED) moveBottomSheet()
    }

    /**
     * Remove an audioRoute from the list
     * @param audioRoute AudioRoute to remove
     */
    fun removeAudioRouteItem(audioRoute: AudioRoute) {
        removeItem(audioRoute)
    }

    /**
     * Add a new audioRoute
     * @param audioRoute AudioRoute to add
     */
    fun addAudioRouteItem(audioRoute: AudioRoute) {
        val position = if (fastAdapter.itemCount == 0) 0
        else onAudioRoutesRequest?.onAudioRoutesRequested()?.indexOf(audioRoute)?.takeIf { it >= 0 }
                ?: 0
        addItem(audioRoute, position)
    }

    override fun slideAnimationUpdate(bottomSheet: BandyerBottomSheet?, slideOffset: Float) {
        super.slideAnimationUpdate(bottomSheet, slideOffset)
        if (!animationEnabled || bottomSheetBehaviour?.lastStableState == state) return
        bottomSheetLayoutContent.lineView?.state = when {
            slideOffset <= 0f -> State.COLLAPSED
            else -> State.EXPANDED
        }
    }

    override fun onExpanded() {
        super.onExpanded()
        bottomSheetLayoutContent.lineView?.state = State.EXPANDED
    }

    override fun onCollapsed() {
        super.onCollapsed()
        bottomSheetLayoutContent.lineView?.state = State.COLLAPSED
    }
}

/**
 * Listener called when the available audioRoute are request to be drawn
 */
interface OnAudioRouteBottomSheetListener {

    /**
     * Return the list  of available audioRoutes
     * @return List<AudioRoute>?
     */
    fun onAudioRoutesRequested(): List<AudioRoute>?
}