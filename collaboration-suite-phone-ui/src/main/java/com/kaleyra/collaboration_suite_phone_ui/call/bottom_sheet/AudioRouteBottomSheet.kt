/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *           
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraClickableBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.AudioRouteState
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.AudioRoute
import com.kaleyra.collaboration_suite_phone_ui.call.buttons.KaleyraLineButton.State
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * AudioRoute BottomSheet to display the available audioRoutes of the device
 * @param context Context
 * @param audioRouteItems items to be shown
 * @param bottomSheetLayoutType bottom sheet layout type
 * @param bottomSheetStyle bottom sheet style
 * @param onAudioRoutesRequest used to request available audioRoutes
 * @constructor
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
class AudioRouteBottomSheet<T : ActionItem>(
    val context: AppCompatActivity,
    audioRouteItems: List<AudioRoute>?,
    bottomSheetLayoutType: BottomSheetLayoutType,
    bottomSheetStyle: Int,
    var onAudioRoutesRequest: OnAudioRouteBottomSheetListener?
) : KaleyraClickableBottomSheet<T>(
    context,
    audioRouteItems as List<T>? ?: listOf<T>(),
    0,
    bottomSheetLayoutType,
    bottomSheetStyle
) {

    /**
     * Returns the current selected Audio Route
     */
    var mCurrentAudioRoute: AudioRoute? = null

    init {
        animationEnabled = true
        recyclerView?.itemAnimator = null
    }

    override fun getClickableViews(viewHolder: RecyclerView.ViewHolder): MutableList<View> {
        val listOfViews = mutableListOf<View>()
        listOfViews.add(viewHolder.itemView)
        return listOfViews
    }

    override fun show() {
        super.show()
        onAudioRoutesRequest?.onAudioRoutesRequested()?.let { setItems(it) }
        bottomSheetBehaviour!!.skipCollapsed = true
        bottomSheetBehaviour!!.isHideable = true
        bottomSheetBehaviour!!.skipAnchor = true
        bottomSheetLayoutContent.backgroundView?.alpha = 1f

        if ((recyclerView!!.layoutManager as? LinearLayoutManager)?.orientation == LinearLayoutManager.HORIZONTAL)
            lineView?.state = State.HIDDEN

        bottomSheetBehaviour!!.disableDragging = bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL

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
    }

    override fun setItems(items: List<ActionItem>) {
        super.setItems(items)
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

    override fun slideAnimationUpdate(bottomSheet: KaleyraBottomSheet?, slideOffset: Float) {
        super.slideAnimationUpdate(bottomSheet, slideOffset)
        if (!animationEnabled || bottomSheetBehaviour?.lastStableState == state) return
        bottomSheetLayoutContent.lineView?.state = when {
            slideOffset <= 0f                                                                 -> State.COLLAPSED
            bottomSheetLayoutType.orientation != BottomSheetLayoutType.Orientation.HORIZONTAL -> State.EXPANDED
            else                                                                              -> this.lineView?.state
        }
    }

    override fun onExpanded() {
        super.onExpanded()
        if (bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL) return
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