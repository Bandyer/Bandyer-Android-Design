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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheet
import com.bandyer.sdk_design.bottom_sheet.BandyerClickableBottomSheet
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_ANCHOR_POINT
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_COLLAPSED
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_DRAGGING
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_EXPANDED
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_HIDDEN
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour.Companion.STATE_SETTLING
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.bottom_sheet.view.BottomSheetLayoutType
import com.bandyer.sdk_design.call.bottom_sheet.items.AudioRoute
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.call.buttons.BandyerLineButton.State
import com.bandyer.sdk_design.extensions.dp2px
import com.bandyer.sdk_design.extensions.getHeightWithVerticalMargin

/**
 * Call BottomSheet to display actions and interact with the call
 * @param context Context
 * @param callActionItems list of actions to display
 * @param bottomSheetLayoutType bottom sheet layout type
 * @param bottomSheetStyle style bottom sheet style
 * @constructor
 * @author kristiyan
 */
@Suppress("UNCHECKED_CAST")
open class CallBottomSheet<T>(
    context: AppCompatActivity,
    private val callActionItems: List<CallAction>,
    bottomSheetLayoutType: BottomSheetLayoutType,
    bottomSheetStyle: Int
) : BandyerClickableBottomSheet<T>(
    context,
    callActionItems as List<T>,
    0,
    bottomSheetLayoutType,
    bottomSheetStyle
) where T : ActionItem {

    private var camera: CallAction.CAMERA? = null
    private var mic: CallAction.MICROPHONE? = null
    private var audioRoute: CallAction.AUDIOROUTE? = null
    private var currentAudioRoute: AudioRoute? = null
    private var fixed: Boolean? = null
    private var animationStartOffset = -1f
    private var animationEndState = -1
    private var lineAnimator: ValueAnimator? = null

    /**
     * Describes if CallBottomSheet can be collapsed
     */
    var collapsible = true
    private var collapsed: Boolean? = null

    private var cameraToggled = false
    private var micToggled = false

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() = calculateBottomSheetDimensions()
    }

    init {
        firstOrNull(CallAction.OPTIONS::class.java).let {
            camera = firstOrNull(CallAction.CAMERA::class.java)
                ?: it?.switchWith as? CallAction.CAMERA
            mic = firstOrNull(CallAction.MICROPHONE::class.java)
                ?: it?.switchWith as? CallAction.MICROPHONE
        }
        recyclerView?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        context.lifecycle.addObserver(lifecycleObserver)
    }

    override fun saveInstanceState(saveInstanceState: Bundle?): Bundle? {
        saveInstanceState?.putString("currentAudioRoute", audioRoute?.mCurrent?.javaClass?.name.toString())
        saveInstanceState?.putString("audioRouteDeviceName", audioRoute?.mCurrent?.name)
        saveInstanceState?.putString("audioRouteDeviceIdentifier", audioRoute?.mCurrent?.identifier)
        saveInstanceState?.putBoolean(
            "audioRouteDeviceIsActive", audioRoute?.mCurrent?.isActive
                ?: false
        )
        saveInstanceState?.putBoolean("cameraIsToggled", camera?.toggled == true)
        saveInstanceState?.putBoolean("micIsToggled", mic?.toggled == true)
        return onSaveInstanceState(saveInstanceState, "action")
    }

    override fun restoreInstanceState(bundle: Bundle?) {
        mContext.get() ?: return
        onRestoreInstanceState(bundle, "action")
        val currentAudioRoute = bundle?.getString("currentAudioRoute") ?: return
        val currentAudioRouteDeviceName = bundle.getString("audioRouteDeviceName") ?: ""
        val currentAudioRouteIdentifier = bundle.getString("audioRouteDeviceIdentifier") ?: ""
        val currentAudioRouteIsActive = bundle.getBoolean("audioRouteDeviceIsActive")
        updateAudioRouteIcon(AudioRoute.getAudioRoute(mContext.get()!!, Class.forName(currentAudioRoute) as Class<AudioRoute>, currentAudioRouteIdentifier, currentAudioRouteDeviceName, currentAudioRouteIsActive))

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

        val isShowingBottomSheetFromBottom = slideOffset <= 0f && fixed == false && bottomSheetBehaviour?.skipCollapsed == false

        bottomSheetLayoutContent.backgroundView?.alpha = if (state == STATE_HIDDEN || isShowingBottomSheetFromBottom) 0f else 1f

        if (bottomSheetBehaviour?.disableDragging == true) {
            bottomSheetLayoutContent.lineView?.state = when (state) {
                STATE_COLLAPSED -> State.COLLAPSED
                else            -> State.HIDDEN
            }
        } else bottomSheetLayoutContent.lineView?.state = when {
            slideOffset > 0f                             -> State.ANCHORED_LINE
            state == STATE_COLLAPSED                     -> State.COLLAPSED
            state == STATE_ANCHOR_POINT || fixed == true -> State.ANCHORED_DOT
            state == STATE_EXPANDED                      -> State.EXPANDED
            state == STATE_DRAGGING && slideOffset > 0f  -> State.ANCHORED_LINE
            else                                         -> bottomSheetLayoutContent.lineView?.state
        }
    }

    override fun onExpanded() {
        super.onExpanded()
        bottomSheetLayoutContent.backgroundView?.alpha = 1f
        if (bottomSheetBehaviour?.disableDragging == true) return
        bottomSheetLayoutContent.lineView?.state = State.EXPANDED
    }

    override fun onDragging() {
        super.onDragging()
        bottomSheetLayoutContent.backgroundView?.alpha = 1f
    }

    override fun onHidden() {
        super.onHidden()
        collapsed = null
        lineAnimator?.removeAllUpdateListeners()
        cameraToggled = camera?.toggled == true
        micToggled = mic?.toggled == true
        if (bottomSheetBehaviour?.disableDragging == true) return
        bottomSheetLayoutContent.lineView?.state = if (bottomSheetBehaviour?.skipCollapsed == true) State.ANCHORED_DOT else State.COLLAPSED
    }

    override fun onCollapsed() {
        super.onCollapsed()
        bottomSheetLayoutContent.lineView?.state = State.COLLAPSED
        if (bottomSheetBehaviour?.skipCollapsed == false) bottomSheetLayoutContent.backgroundView?.alpha = 0f
    }

    override fun onAnchor() {
        super.onAnchor()
        bottomSheetLayoutContent.backgroundView?.alpha = 1f
        if (bottomSheetBehaviour?.disableDragging == true) return
        bottomSheetLayoutContent.lineView?.state = if (bottomSheetBehaviour?.skipCollapsed == true) State.ANCHORED_DOT else State.ANCHORED_LINE
    }

    /**
     * Update the currentAudio route displayed in the actions
     * @param audioRoute new AudioRoute? to be displayed
     */
    fun updateAudioRouteIcon(audioRoute: AudioRoute?) {
        this@CallBottomSheet.audioRoute = firstOrNull(CallAction.AUDIOROUTE::class.java)
        this@CallBottomSheet.audioRoute?.setCurrent(audioRoute)
        currentAudioRoute = audioRoute
    }

    override fun onConfigurationChanged() {
        bottomSheetBehaviour ?: return
        fixed ?: return
        calculateBottomSheetDimensions()
    }

    private fun setup(collapsible: Boolean, fixed: Boolean? = false, collapsed: Boolean = false) = bottomSheetLayoutContent.post {
        val layoutManager = bottomSheetLayoutContent.recyclerView?.layoutManager as? LinearLayoutManager
        val notVerticallyDraggable = bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL || layoutManager?.findLastCompletelyVisibleItemPosition() ?: callActionItems.size < callActionItems.size

        bottomSheetBehaviour ?: return@post
        this.fixed = fixed!!
        animationStartOffset = -1f
        animationEndState = -1
        animationEnabled = fixed == false
        this.collapsible = collapsible
        this.collapsed = collapsed
        bottomSheetBehaviour!!.disableDragging = notVerticallyDraggable
        if (notVerticallyDraggable) lineView?.state = State.HIDDEN

        if (fixed == true) {
            bottomSheetBehaviour!!.isHideable = false
            bottomSheetBehaviour!!.skipCollapsed = true
            bottomSheetBehaviour!!.disableDragging = true
            expand()
            return@post
        }

        calculateBottomSheetDimensions()

        lineView?.setOnClickListener {
            bottomSheetLayoutContent.backgroundView?.alpha = 1f
            bottomSheetLayoutContent.lineView?.state = State.ANCHORED_LINE
            if (state == STATE_COLLAPSED)
                anchor()
            else if (bottomSheetBehaviour?.skipCollapsed == true)
                expand()
        }

        camera?.toggle(cameraToggled)
        mic?.toggle(micToggled)
    }

    private fun calculateBottomSheetDimensions() {
        var peekHeight: Int
        var anchorOffset: Int

        val firstItem = recyclerView?.layoutManager?.getChildAt(0)

        firstItem?.post {
            if (collapsed == null) collapsed = bottomSheetBehaviour?.skipCollapsed != true

            bottomSheetBehaviour ?: kotlin.run {
                dispose()
                return@post
            }

            val layoutManager = recyclerView?.layoutManager ?: return@post

            val oneLineHeightPadding = when {
                layoutManager is LinearLayoutManager && layoutManager.orientation == LinearLayoutManager.HORIZONTAL -> firstItem.paddingBottom
                layoutManager is GridLayoutManager                                                                  -> firstItem.paddingTop.takeIf { callActionItems.size > layoutManager.spanCount } ?: 0
                else                                                                                                -> 0
            }

            val oneLineHeight = (lineView?.getHeightWithVerticalMargin().takeIf { lineView?.visibility != View.GONE } ?: 0) +
                    (titleView?.getHeightWithVerticalMargin().takeIf { lineView?.visibility != View.GONE } ?: 0) +
                    firstItem.getHeightWithVerticalMargin() + oneLineHeightPadding

            when {
                collapsible -> {
                    peekHeight = bottomSheetLayoutContent.rootView.top + (lineView?.getHeightWithVerticalMargin().takeIf { lineView?.visibility != View.GONE }
                        ?: 0)
                    anchorOffset = oneLineHeight
                    animationEndState = if (collapsed!! && collapsible) STATE_COLLAPSED else STATE_ANCHOR_POINT
                    bottomSheetBehaviour!!.skipCollapsed = false
                }
                else        -> {
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
                val shouldExpand = bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL ||
                        (bottomSheetLayoutType is BottomSheetLayoutType.GRID && bottomSheetLayoutType.spanSize >= callActionItems.size)
                val newState = if (shouldExpand) STATE_EXPANDED else if (collapsed!! && collapsible) STATE_COLLAPSED else STATE_ANCHOR_POINT
                if (state == newState) {
                    when (newState) {
                        STATE_COLLAPSED    -> onCollapsed()
                        STATE_ANCHOR_POINT -> onAnchor()
                    }
                }
                if (state == STATE_HIDDEN && state != STATE_SETTLING && state != newState) state = newState
            }

            if (bottomSheetBehaviour?.disableDragging == true) {
                lineView?.state = State.HIDDEN
            } else bottomSheetLayoutContent.lineView?.state = when {
                bottomSheetBehaviour!!.skipCollapsed && state == STATE_ANCHOR_POINT || fixed == true -> State.ANCHORED_DOT
                state == STATE_ANCHOR_POINT                                                          -> State.ANCHORED_LINE
                state == STATE_EXPANDED                                                              -> State.EXPANDED
                state == STATE_SETTLING                                                              -> bottomSheetLayoutContent.lineView?.state
                else                                                                                 -> State.COLLAPSED
            }

            if (animationEndState == -1) return@post
            animationStartOffset = bottomSheetBehaviour!!.getStableStateSlideOffset(animationEndState)
        }
    }
}