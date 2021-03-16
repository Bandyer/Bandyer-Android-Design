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

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheet
import com.bandyer.sdk_design.bottom_sheet.BandyerClickableBottomSheet
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_ANCHOR_POINT
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_COLLAPSED
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_EXPANDED
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutContent
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.buttons.BandyerLineButton
import com.bandyer.sdk_design.call.buttons.BandyerLineButton.State
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.getHeightWithVerticalMargin

/**
 * Call BottomSheet to display actions and interact with the call
 * @param context Context
 * @param callActionItems list of actions to display
 * @constructor
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
open class CallBottomSheet<T>(val context: AppCompatActivity,
                              private val callActionItems: List<CallAction>,
                              bottomSheetStyle: Int) : BandyerClickableBottomSheet<T>(
        context,
        callActionItems as List<T>,
        callActionItems.size.takeIf { it < MAX_ITEMS_PER_ROW } ?: MAX_ITEMS_PER_ROW,
        0,
        BottomSheetLayoutType.GRID,
        bottomSheetStyle) where T : ActionItem {

    private var camera: CallAction.CAMERA? = null
    private var mic: CallAction.MICROPHONE? = null
    private var audioRoute: CallAction.AUDIOROUTE? = null
    private var currentAudioRoute: AudioRoute? = null

    private var animationStartOffset = -1f
    private var animationEndState = -1
    private var lineAnimator: ValueAnimator? = null

    /**
     * Describes if CallBottomSheet can be collapsed
     */
    var collapsible = true

    private var cameraToggled = false
    private var micToggled = false

    /**
     * Singleton of the call bottom sheet
     */
    companion object {
        /**
         * Max visible items per row
         */
        const val MAX_ITEMS_PER_ROW = 4
    }

    init {
        firstOrNull(CallAction.OPTIONS::class.java).let {
            camera = firstOrNull(CallAction.CAMERA::class.java)
                    ?: it?.switchWith as? CallAction.CAMERA
            mic = firstOrNull(CallAction.MICROPHONE::class.java)
                    ?: it?.switchWith as? CallAction.MICROPHONE
        }
        recyclerView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    }

    override fun saveInstanceState(saveInstanceState: Bundle?): Bundle? {
        saveInstanceState?.putString("currentAudioRoute", audioRoute?.mCurrent?.javaClass?.name.toString())
        saveInstanceState?.putString("audioRouteDeviceName", audioRoute?.mCurrent?.name)
        saveInstanceState?.putString("audioRouteDeviceIdentifier", audioRoute?.mCurrent?.identifier)
        saveInstanceState?.putBoolean("audioRouteDeviceIsActive", audioRoute?.mCurrent?.isActive
                ?: false)
        saveInstanceState?.putBoolean("cameraIsToggled", camera?.toggled == true)
        saveInstanceState?.putBoolean("micIsToggled", mic?.toggled == true)
        return onSaveInstanceState(saveInstanceState, "action")
    }

    override fun restoreInstanceState(bundle: Bundle?) {
        onRestoreInstanceState(bundle, "action")
        val currentAudioRoute = bundle?.getString("currentAudioRoute") ?: return
        val currentAudioRouteDeviceName = bundle.getString("audioRouteDeviceName") ?: ""
        val currentAudioRouteIdentifier = bundle.getString("audioRouteDeviceIdentifier") ?: ""
        val currentAudioRouteIsActive = bundle.getBoolean("audioRouteDeviceIsActive")
        updateAudioRouteIcon(AudioRoute.getAudioRoute(context, Class.forName(currentAudioRoute) as Class<AudioRoute>, currentAudioRouteIdentifier, currentAudioRouteDeviceName, currentAudioRouteIsActive))

        bundle.getBoolean("micIsToggled", false).let {
            mic?.toggle(it)
        }

        bundle.getBoolean("cameraIsToggled", false).let {
            camera?.toggle(it)
        }
    }


    /**
     * Collapse the bottomSheet
     */
    fun collapse() {
        collapse(lineView, mContext.get()?.dp2px(15f))
    }

    /**
     * Show the bottomSheet
     * @param collapsible true if it should be collapsible, false otherwise
     * @param fixed true if it should not move, false otherwise
     * @param collapsed optional initial collapsed state
     */
    fun show(collapsible: Boolean, fixed: Boolean? = false, collapsed: Boolean = false) {
        super.show()
        setup(collapsible, fixed, collapsed)
    }

    override fun slideAnimationReady(bottomSheet: BandyerBottomSheet?, state: Int, slideOffset: Float) {
        super.slideAnimationReady(bottomSheet, state, slideOffset)
        if (!animationEnabled) return
        if (state == animationEndState) animationStartOffset = slideOffset
    }

    override fun slideAnimationUpdate(bottomSheet: BandyerBottomSheet?, slideOffset: Float) {
        super.slideAnimationUpdate(bottomSheet, slideOffset)
        if (!animationEnabled || bottomSheetBehaviour?.lastStableState == state) return
        bottomSheetLayoutContent.lineView?.state = when {
            slideOffset <= 0f -> BandyerLineButton.State.COLLAPSED
            bottomSheetBehaviour?.skipCollapsed == true -> State.ANCHORED_DOT
            else -> State.ANCHORED_LINE
        }
    }

    override fun onSettling() {
        super.onSettling()
        if (bottomSheetBehaviour?.lastStableState != BandyerBottomSheetBehaviour.STATE_HIDDEN && bottomSheetLayoutContent.visibility == View.VISIBLE)
            bottomSheetLayoutContent.backgroundView?.alpha = 1f
    }

    override fun onExpanded() {
        super.onExpanded()
        replaceOptionItem()
        bottomSheetLayoutContent.lineView?.state = BandyerLineButton.State.EXPANDED
    }

    override fun onDragging() {
        super.onDragging()
        bottomSheetLayoutContent.backgroundView?.alpha = 1f
    }

    override fun onHidden() {
        super.onHidden()
        lineAnimator?.removeAllUpdateListeners()
        replaceOptionItem(true)
        cameraToggled = camera?.toggled == true
        micToggled = mic?.toggled == true
    }

    override fun onCollapsed() {
        super.onCollapsed()
        replaceOptionItem(true)
        bottomSheetLayoutContent.lineView?.state = BandyerLineButton.State.COLLAPSED
        bottomSheetLayoutContent.backgroundView?.alpha = 0f
    }

    override fun onAnchor() {
        super.onAnchor()
        replaceOptionItem(true)
        bottomSheetLayoutContent.lineView?.state =
                if (bottomSheetBehaviour?.skipCollapsed == true) State.ANCHORED_DOT
                else State.ANCHORED_LINE
    }

    /**
     * Update the currentAudio route displayed in the actions
     * @param audioRoute new AudioRoute? to be displayed
     */
    fun updateAudioRouteIcon(audioRoute: AudioRoute?) {
        this.audioRoute = firstOrNull(CallAction.AUDIOROUTE::class.java)
        this.audioRoute?.setCurrent(audioRoute)
        currentAudioRoute = audioRoute
        if (state == STATE_EXPANDED) replaceOptionItem()
    }

    private fun replaceOptionItem(withOptionActionItem: Boolean = false) {
        val options = callActionItems.firstOrNull { it is CallAction.OPTIONS } as? CallAction.OPTIONS
                ?: return

        val oldItem = firstOrNull(options.switchWith::class.java) ?: options

        if (withOptionActionItem && oldItem is CallAction.OPTIONS) return

        val newItem = if (!withOptionActionItem) options.switchWith else options

        (newItem as? CallAction.AUDIOROUTE)?.setCurrent(currentAudioRoute)

        replaceItems(oldItem, newItem)
    }

    private fun setup(collapsible: Boolean, fixed: Boolean? = false, collapsed: Boolean = false) = bottomSheetLayoutContent.post contentPost@{
        animationStartOffset = -1f
        animationEndState = -1
        animationEnabled = fixed == false
        this.collapsible = collapsible
        bottomSheetBehaviour!!.disableDragging = callActionItems.size <= MAX_ITEMS_PER_ROW

        if (fixed == true) {
            bottomSheetBehaviour!!.isHideable = false
            bottomSheetBehaviour!!.skipCollapsed = true
            bottomSheetBehaviour!!.disableDragging = true
            expand()
            return@contentPost
        }

        var peekHeight: Int
        var anchorOffset: Int

        val firstItem = recyclerView?.layoutManager?.getChildAt(0)

        firstItem?.post {
            val oneLineHeight = (lineView?.getHeightWithVerticalMargin() ?: 0) +
                    (titleView?.getHeightWithVerticalMargin() ?: 0) +
                    firstItem.getHeightWithVerticalMargin() +  (firstItem.paddingTop.takeIf { callActionItems.size > MAX_ITEMS_PER_ROW } ?: 0)

            when {
                collapsible -> {
                    peekHeight = bottomSheetLayoutContent.rootView.top + (lineView?.getHeightWithVerticalMargin()
                            ?: 0)
                    anchorOffset = oneLineHeight
                    animationEndState = if (collapsed && collapsible) STATE_COLLAPSED else STATE_ANCHOR_POINT
                    bottomSheetBehaviour!!.skipCollapsed = false
                }
                else -> {
                    peekHeight = oneLineHeight
                    anchorOffset = peekHeight
                    bottomSheetBehaviour!!.skipCollapsed = true
                }
            }

            with(bottomSheetBehaviour!!) {
                this.peekHeight = peekHeight
                this.anchorOffset = anchorOffset
                skipAnchor = false
                bottomSheetBehaviour!!.isHideable = false
                state = if(callActionItems.size <= MAX_ITEMS_PER_ROW) STATE_EXPANDED else if (collapsed && collapsible) STATE_COLLAPSED else STATE_ANCHOR_POINT
            }

            if (animationEndState == -1) return@post
            animationStartOffset = bottomSheetBehaviour!!.getStableStateSlideOffset(animationEndState)
        }

        bottomSheetLayoutContent.lineView?.state =
                if (state == STATE_COLLAPSED || bottomSheetBehaviour?.skipCollapsed == true) State.ANCHORED_DOT
                else State.ANCHORED_LINE

        if (collapsed && collapsible) bottomSheetLayoutContent.backgroundView?.alpha = 0f

        if (callActionItems.size  <= MAX_ITEMS_PER_ROW) {
            lineView?.layoutParams?.height = context.dp2px(24f)
            lineView?.visibility = View.INVISIBLE
            lineView?.isClickable = false
            lineView?.requestLayout()
        }

        lineView?.setOnClickListener {
            if (state == STATE_COLLAPSED)
                anchor()
            else if (bottomSheetBehaviour?.skipCollapsed == true)
                expand()
        }

        camera?.toggle(cameraToggled)
        mic?.toggle(micToggled)
    }
}