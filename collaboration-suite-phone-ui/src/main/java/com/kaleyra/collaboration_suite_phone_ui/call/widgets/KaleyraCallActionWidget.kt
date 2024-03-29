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

package com.kaleyra.collaboration_suite_phone_ui.call.widgets

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getScreenSize
import com.kaleyra.collaboration_suite_core_ui.widget.HideableWidget
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.BaseKaleyraBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraActionBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.KaleyraBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.OnStateChangedBottomSheetListener
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.behaviours.KaleyraBottomSheetBehaviour
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutContent
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.AudioRouteBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.CallBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.OnAudioRouteBottomSheetListener
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.RingingBottomSheet
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.AudioRoute
import com.kaleyra.collaboration_suite_phone_ui.call.bottom_sheet.items.CallAction
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCallThemeAttribute
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Widget used during a call to perform actions such as mute video/audio, change audioRoute etc.
 * @param context Context
 * @param coordinatorLayout CoordinatorLayout
 * @param callActionItems items to use
 * @constructor
 * @author kristiyan
 */
class KaleyraCallActionWidget<T, F>(
    val context: AppCompatActivity,
    val coordinatorLayout: CoordinatorLayout,
    private var actions: List<CallAction>) :
    HideableWidget where T : ActionItem, F : KaleyraBottomSheet {

    val callActionItems: List<CallAction>
        get() = actions

    /**
     * @suppress
     */
    override var hidingTimer: CountDownTimer? = null

    /**
     * @suppress
     */
    override var millisUntilTimerFinish: Long = 0
    /**
     * Click listener for when an item has been clicked
     */
    var onClickListener: OnClickListener? = null

    /**
     * Used to request available audioRoutes
     */
    var onAudioRoutesRequest: OnAudioRouteBottomSheetListener? = null

    private var anchoredViews: MutableList<Pair<View, Int>> = mutableListOf()
    private var currentBottomSheetLayout: BottomSheetLayoutContent? = null

    private var currentCallBottomSheetLayoutType: BottomSheetLayoutType? = null

    private var currentShownBottomSheet: BaseKaleyraBottomSheet? = null

    private var isDraggingEnabled = true

    private var mCurrentAudioRoute: AudioRoute? = null

    private var callBottomSheet: CallBottomSheet<T>? = null

    private var ringingBottomSheet: RingingBottomSheet<T>? = null

    private var audioRouteBottomSheet: AudioRouteBottomSheet<T>? = null

    /**
     * Returns true if the widget is collapsible, false otherwise.
     */
    var collapsible: Boolean = true

    private var fixed: Boolean = true

    /**
     * Optional item decoration to be added on action items' recycler view
     */
    var itemDecoration: RecyclerView.ItemDecoration? = null
        set(value) {
            if (value != null) {
                field = value
                currentShownBottomSheet?.let { addItemDecoration(it) }
                return
            }
            currentShownBottomSheet?.let { removeItemDecoration(it) }
            field = null
        }

    /**
     * Sliding listener for when the widget has been slided
     */
    var slidingListener: SlidingListener? = null

    /**
     * Current widget hidden state
     */
    var isHidden = false

    /**
     * Current onShowListener
     */
    private var onShowListener: OnShowListener? = null

    /**
     * Current onHiddenListener
     */
    private var onHiddenListener: OnHiddenListener? = null

    /**
     * Widget states
     */
    enum class KaleyraCallActionWidgetState {
        /**
         * Fully expanded
         */
        EXPANDED,

        /**
         * Anchored
         */
        ANCHORED,

        /**
         * Collapsed
         */
        COLLAPSED
    }

    private val onBottomSheetAction = KaleyraActionBottomSheet.OnActionBottomSheetListener<ActionItem, KaleyraBottomSheet> action@{ bottomSheet, action, position ->
        val consumed = when (action) {
            is AudioRoute -> onClickListener?.onAudioRouteClicked(action, position)
            is CallAction -> onClickListener?.onCallActionClicked(action, position)
            else -> false
        }
        if (consumed != false) return@action true
        when (action) {
            is AudioRoute -> {
                callBottomSheet?.updateAudioRouteIcon(mCurrentAudioRoute)
                bottomSheet as AudioRouteBottomSheet<*>
                bottomSheet.mCurrentAudioRoute = action as AudioRoute
                bottomSheet.hide(true)
            }
            is CallAction.AUDIOROUTE -> showAudioRouteBottomSheet()
            is CallAction.OPTIONS -> expand()
            else -> return@action false
        }
        true
    }

    private val onBottomSheetCallBacks = object : OnStateChangedBottomSheetListener<KaleyraBottomSheet> {

        override fun onExpand(bottomSheet: KaleyraBottomSheet) {
            disableAutoHide()
            state = KaleyraCallActionWidgetState.EXPANDED
            onShowListener?.let {
                it.onShown()
                onShowListener = null
            }
            slidingListener?.onStateChanged(KaleyraCallActionWidgetState.EXPANDED)
        }

        override fun onAnchor(bottomSheet: KaleyraBottomSheet) {
            if (bottomSheet is CallBottomSheet<*>) hidingTimer?.start()
            state = KaleyraCallActionWidgetState.ANCHORED
            slidingListener?.onSlide(
                currentBottomSheetLayout?.top
                    ?: context.getScreenSize().y, false
            )
            onShowListener?.let {
                it.onShown()
                onShowListener = null
            }
            slidingListener?.onStateChanged(KaleyraCallActionWidgetState.ANCHORED)
        }

        override fun onShow(bottomSheet: KaleyraBottomSheet) {
            currentShownBottomSheet = bottomSheet as BaseKaleyraBottomSheet
            anchorViews()
            (bottomSheet as? CallBottomSheet<*>)?.updateAudioRouteIcon(mCurrentAudioRoute)
            addItemDecoration(bottomSheet)
        }

        override fun onHide(bottomSheet: KaleyraBottomSheet) {
            when (bottomSheet) {
                is CallBottomSheet<*> -> disableAutoHide()
                is AudioRouteBottomSheet<*> -> {
                    if (!isHidden) showCallControls(collapsible, fixed, bottomSheetLayoutType = currentCallBottomSheetLayoutType)
                    disposeBottomSheet(bottomSheet)
                }
                is RingingBottomSheet<*> -> disposeBottomSheet(bottomSheet)
            }
            removeItemDecoration(bottomSheet)
            onHiddenListener?.onHidden()
            onHiddenListener = null
        }

        override fun onDragging(bottomSheet: KaleyraBottomSheet) {
            disableAutoHide()
        }

        override fun onCollapse(bottomSheet: KaleyraBottomSheet) {
            hidingTimer?.onFinish()
            state = KaleyraCallActionWidgetState.COLLAPSED
            slidingListener?.onSlide(
                currentBottomSheetLayout?.top
                    ?: context.getScreenSize().y, true
            )

            if (bottomSheet is CallBottomSheet<*>) {
                onShowListener?.onShown()
                onShowListener = null
            }
            slidingListener?.onStateChanged(KaleyraCallActionWidgetState.COLLAPSED)
        }

        override fun onSlide(bottomSheet: KaleyraBottomSheet, slideOffset: Float) {
            if (slideOffset == 0.0f) return
            val top = currentBottomSheetLayout?.top ?: return
            if (currentShownBottomSheet!!.state == KaleyraBottomSheetBehaviour.STATE_SETTLING || currentShownBottomSheet!!.state == KaleyraBottomSheetBehaviour.STATE_DRAGGING)
                disableAutoHide()
            slidingListener?.onSlide(
                top,
                (currentShownBottomSheet?.state == KaleyraBottomSheetBehaviour.STATE_COLLAPSED || currentShownBottomSheet?.state == KaleyraBottomSheetBehaviour.STATE_HIDDEN)
            )
        }
    }

    private val onAudioRoutesListener = object : OnAudioRouteBottomSheetListener {
        override fun onAudioRoutesRequested(): List<AudioRoute>? {
            return onAudioRoutesRequest?.onAudioRoutesRequested()
        }
    }

    init {
        callActionItems.forEachIndexed { index, callAction ->
            if (index < callActionItems.size - 1) {
                callAction.itemView?.nextFocusForwardId =
                    callActionItems[index + 1].itemView?.id!!
            }
        }
    }

    /**
     * Current widget state
     */
    @Suppress("SetterBackingFieldAssignment")
    var state: KaleyraCallActionWidgetState? = null

    /**
     * Save current instance state
     * @param saveInstanceState Bundle? to store in
     * @return Bundle updated
     */
    fun saveInstanceState(saveInstanceState: Bundle?): Bundle? {
        val bundle = audioRouteBottomSheet?.saveInstanceState(saveInstanceState)
        return callBottomSheet?.saveInstanceState(bundle)
    }

    /**
     * Disable dragging capability of shown bottomsheet
     */
    fun disableDragging() {
        isDraggingEnabled = false
        currentShownBottomSheet?.bottomSheetBehaviour?.disableDragging = !isDraggingEnabled
    }

    /**
     * Enable dragging capability of shown bottomsheet
     */
    fun enableDragging() {
        isDraggingEnabled = true
        currentShownBottomSheet?.bottomSheetBehaviour?.disableDragging = !isDraggingEnabled
    }

    /**
     * Restore a previously saved instance state
     * @param bundle Bundle? to restore from
     */
    fun restoreInstanceState(bundle: Bundle?) {
        callBottomSheet?.restoreInstanceState(bundle)
        audioRouteBottomSheet?.restoreInstanceState(bundle)
    }

    /**
     * Toggle the widget.
     */
    fun toggle() {
        if (callBottomSheet?.isVisible() == true)
            callBottomSheet?.toggle()
        if (audioRouteBottomSheet?.isVisible() == true)
            audioRouteBottomSheet?.toggle()
    }

    /**
     * Expand the widget.
     */
    fun expand() {
        if (callBottomSheet?.isVisible() == true)
            callBottomSheet?.expand()
        if (audioRouteBottomSheet?.isVisible() == true)
            audioRouteBottomSheet?.expand()
    }

    /**
     * Request focus on current bottom sheet shown.
     */
    fun requestFocus(): View? {
        val toBeFocused = when {
            ringingBottomSheet?.isVisible() == true -> ringingBottomSheet?.bottomSheetLayoutContent?.recyclerView?.layoutManager?.findViewByPosition(1)
            callBottomSheet?.isVisible() == true -> callBottomSheet?.bottomSheetLayoutContent?.recyclerView?.getChildAt(0)
            audioRouteBottomSheet?.isVisible() == true -> audioRouteBottomSheet?.bottomSheetLayoutContent?.recyclerView?.getChildAt(0)
            else -> null
        }
        toBeFocused?.requestFocus()
        return toBeFocused
    }

    /**
     * Collapse the widget.
     */
    fun collapse() {
        if (callBottomSheet?.isVisible() == true) when {
            collapsible -> callBottomSheet?.collapse()
            callBottomSheet?.bottomSheetLayoutType?.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL
                    && callBottomSheet?.bottomSheetBehaviour?.disableDragging == true -> callBottomSheet?.expand()
            else -> callBottomSheet?.anchor()
        }

        if (audioRouteBottomSheet?.isVisible() == true)
            audioRouteBottomSheet?.hide()
    }

    /**
     * Hides current displayed bottomsheet
     */
    @JvmOverloads
    fun hide(onHiddenListener: OnHiddenListener? = null) {
        if (isHidden) {
            onHiddenListener?.onHidden()
            return
        }
        isHidden = true
        if (onHiddenListener != null) this.onHiddenListener = onHiddenListener
        when (currentShownBottomSheet) {
            callBottomSheet -> {
                callBottomSheet?.hide(true)
            }
            else -> disposeBottomSheet(currentShownBottomSheet)
        }
    }

    /**
     * Shows Kaleyra call action widget based on previous last bottom sheet shown
     */
    @JvmOverloads
    fun show(onShowListener: OnShowListener? = null) {
        if (!isHidden) {
            onShowListener?.onShown()
            return
        }
        isHidden = false
        if (onShowListener != null) this.onShowListener = onShowListener
        when (currentShownBottomSheet) {
            ringingBottomSheet -> showRingingControls()
            else -> showCallControls(collapsible, fixed, collapsed = true, currentCallBottomSheetLayoutType)
        }
    }

    /**
     * Shows Kaleyra call action widget based on previous last bottom sheet shown
     * delayed by factor
     * @param millis delay factor in milliseconds
     */
    fun showDelayed(millis: Long, onShowListener: OnShowListener? = null) {
        MainScope().launch {
            delay(millis)
            show(onShowListener)
        }
    }

    /**
     * Check expanded status.
     * @return true if widget is expanded, false otherwise.
     */
    fun isExpanded(): Boolean {
        return when {
            ringingBottomSheet?.isVisible() == true -> true
            callBottomSheet?.isVisible() == true -> callBottomSheet?.isExpanded() == true
            audioRouteBottomSheet?.isVisible() == true -> audioRouteBottomSheet?.isExpanded() == true
            else -> false
        }
    }

    /**
     * Check collapsed status.
     * @return true if widget is expanded, false otherwise.
     */
    fun isCollapsed(): Boolean {
        return when {
            ringingBottomSheet?.isVisible() == true -> false
            callBottomSheet?.isVisible() == true -> callBottomSheet?.isCollapsed() == true
            audioRouteBottomSheet?.isVisible() == true -> audioRouteBottomSheet?.isCollapsed() == true
            else -> false
        }
    }

    /**
     * Check anchored status.
     * @return true if widget is anchored, false otherwise.
     */
    fun isAnchored(): Boolean {
        return when {
            ringingBottomSheet?.isVisible() == true -> true
            callBottomSheet?.isVisible() == true -> callBottomSheet?.isAnchored() == true
            audioRouteBottomSheet?.isVisible() == true -> audioRouteBottomSheet?.isAnchored() == true
            else -> false
        }
    }

    /**
     * Check if the call controls are currently shown.
     * @return true if shown, false otherwise
     */
    fun isShowingCallControls(): Boolean = currentShownBottomSheet is CallBottomSheet<*>

    /**
     * Check if the audio output route controls are currently shown.
     * @return true if shown, false otherwise
     */
    fun isShowingAudioRouteControls(): Boolean = currentShownBottomSheet is AudioRouteBottomSheet<*>

    /**
     * Check if the ringing controls are currently shown.
     * @return true if shown, false otherwise
     */
    fun isShowingRingingControls(): Boolean = currentShownBottomSheet is RingingBottomSheet<*>

    /**
     * Anchor a view to the widget
     * @param anchoredView View to anchor
     * @param gravity gravity to apply
     * @param forceAnchor true to anchor no matter what, false otherwise
     */
    @JvmOverloads
    fun setAnchoredView(anchoredView: View, gravity: Int) {
        var hasAnchoredView = false
        anchoredViews.forEach {
            if (it.first == anchoredView) {
                hasAnchoredView = true
                return@forEach
            }
        }
        if (!hasAnchoredView) {
            anchoredViews.add(Pair(anchoredView, gravity))
            checkAnchoredViewInCoordinatorLayout()
        }
    }

    private fun anchorViews() = currentBottomSheetLayout?.post {
        currentBottomSheetLayout?.parent ?: return@post
        removeAnchorFromAnchoredView()
        anchoredViews.forEach { pair ->
            val lp = (pair.first.layoutParams as CoordinatorLayout.LayoutParams)
            lp.anchorId = currentBottomSheetLayout?.id ?: View.NO_ID
            lp.anchorGravity = pair.second
            lp.gravity = pair.second
            pair.first.layoutParams = lp
            pair.first.alpha = 1f
        }
    }

    /**
     * Anchors current bottom sheet
     */
    fun anchor() {
        currentShownBottomSheet?.anchor()
    }

    /**
     * Hide call controls
     */
    fun hideCallControls(withTimer: Boolean = false) {
        if (!withTimer) {
            onHidingTimerFinished()
            hidingTimer?.cancel()
            return
        }
        if (currentShownBottomSheet is CallBottomSheet<*>) hidingTimer?.start()
    }

    private fun checkAnchoredViewInCoordinatorLayout() {
        if (currentBottomSheetLayout == null) return
        anchoredViews.forEach { pair ->
            if (pair.second == Gravity.TOP)
                (pair.first.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = pair.first.height
        }
    }

    private fun removeAnchorFromAnchoredView() {
        anchoredViews.forEach { pair ->
            if (pair.first.parent == null || pair.first.parent !is CoordinatorLayout)
                return
            val lp = (pair.first.layoutParams as CoordinatorLayout.LayoutParams)
            lp.anchorId = View.NO_ID
            lp.anchorGravity = Gravity.TOP
            lp.gravity = Gravity.BOTTOM or Gravity.END
            pair.first.layoutParams = lp
            pair.first.alpha = 0f
        }
    }


    /**
     * Show the call controls
     * @param collapsible true if the bottomSheet should be collapsible, false otherwise
     * @param fixed true if it can not be moved, false otherwise
     * @param collapsed optional initial state collapsed
     * @param bottomSheetLayoutType BottomSheetLayoutType? optional bottom sheet layout type
     * @return Boolean
     */
    @JvmOverloads
    fun showCallControls(
        collapsible: Boolean,
        fixed: Boolean = false,
        collapsed: Boolean = false,
        bottomSheetLayoutType: BottomSheetLayoutType? =
            BottomSheetLayoutType.GRID(
                if (callActionItems.size > DEFAULT_ACTION_ITEM_GRID_SPAN_SIZE) DEFAULT_ACTION_ITEM_GRID_SPAN_SIZE
                else callActionItems.size, BottomSheetLayoutType.Orientation.VERTICAL)) = coordinatorLayout.post {
        createCallBottomSheet(bottomSheetLayoutType!!)
        isHidden = false
        currentBottomSheetLayout = callBottomSheet?.bottomSheetLayoutContent
        disposeBottomSheet(ringingBottomSheet)
        disposeBottomSheet(audioRouteBottomSheet)
        this.collapsible = collapsible
        this.fixed = fixed
        callBottomSheet?.bottomSheetLayoutContent?.id = R.id.kaleyra_id_bottom_sheet_call
        callBottomSheet?.show(collapsible, fixed, collapsed)
        callBottomSheet?.updateAudioRouteIcon(mCurrentAudioRoute)
    }

    /**
     * Show ringing controls like Hangup and Answer
     * @param bottomSheetLayoutType BottomSheetLayoutType? optional bottom sheet layout type
     * @return Boolean
     */
    fun showRingingControls(bottomSheetLayoutType: BottomSheetLayoutType? = BottomSheetLayoutType.GRID(2, BottomSheetLayoutType.Orientation.VERTICAL)) = coordinatorLayout.post {
        createRingingBottomSheet(bottomSheetLayoutType!!)
        isHidden = false
        currentBottomSheetLayout = ringingBottomSheet?.bottomSheetLayoutContent
        ringingBottomSheet?.bottomSheetLayoutContent?.id = R.id.kaleyra_id_bottom_sheet_ringing
        if (callBottomSheet?.isVisible() == true || audioRouteBottomSheet?.isVisible() == true) {
            disposeBottomSheet(callBottomSheet)
            disposeBottomSheet(audioRouteBottomSheet)
        }
        ringingBottomSheet?.show()
    }

    /**
     * Show the audioRoute bottomSheet
     * @param bottomSheetLayoutType BottomSheetLayoutType? optional bottom sheet layout type
     * @return Boolean
     */
    fun showAudioRouteBottomSheet(bottomSheetLayoutType: BottomSheetLayoutType? = BottomSheetLayoutType.LIST(BottomSheetLayoutType.Orientation.VERTICAL)) = coordinatorLayout.post {
        createAudioRouteBottomSheet(bottomSheetLayoutType!!)
        isHidden = false
        audioRouteBottomSheet?.bottomSheetLayoutContent?.id = R.id.kaleyra_id_bottom_sheet_audio_route
        callBottomSheet?.hide(true)
        currentBottomSheetLayout = audioRouteBottomSheet?.bottomSheetLayoutContent
        audioRouteBottomSheet?.show()
    }

    /**
     * @suppress
     */
    override fun onHidingTimerFinished() {
        if (!collapsible) return
        callBottomSheet?.collapse()
    }

    private fun addItemDecoration(bottomSheet: KaleyraBottomSheet) = itemDecoration
        ?.takeIf { bottomSheet.recyclerView?.itemDecorationCount == 0 }
        ?.let { bottomSheet.recyclerView!!.addItemDecoration(it) }

    private fun removeItemDecoration(bottomSheet: KaleyraBottomSheet) =
        itemDecoration?.let { bottomSheet.recyclerView?.removeItemDecoration(it) }

    /**
     * Add an audioRoute item to the list of available routes
     * @param audioRoute AudioRoute to add
     */
    fun addAudioRouteItem(audioRoute: AudioRoute) {
        audioRouteBottomSheet?.addAudioRouteItem(audioRoute)
    }

    /**
     * Select an audioRoute item from the list of available routes
     * @param audioRoute AudioRoute? to select
     */
    fun selectAudioRoute(audioRoute: AudioRoute?) {
        audioRoute ?: return
        if (mCurrentAudioRoute == audioRoute) return
        mCurrentAudioRoute = audioRoute
        audioRouteBottomSheet?.selectAudioRoute(audioRoute)
        callBottomSheet?.updateAudioRouteIcon(audioRoute)
    }

    /**
     * Update an audioRoute item from the list of available routes
     * @param audioRoute AudioRoute? to select
     */
    fun updateAudioRoute(audioRoute: AudioRoute) =
        if (audioRouteBottomSheet?.isVisible() == true) audioRouteBottomSheet?.updateItem(audioRoute) else Unit

    /**
     * Set audio route items to be displayed
     * @param items List<AudioRoute> items to be displayed
     */
    fun setAudioRouteItems(items: List<AudioRoute>) =
        if (audioRouteBottomSheet?.isVisible() == true) audioRouteBottomSheet?.setItems(items) else Unit

    /**
     * Remove an audioRoute item from the list of available routes
     * @param audioRoute AudioRoute to remove
     */
    fun removeAudioRouteItem(audioRoute: AudioRoute) = audioRouteBottomSheet?.removeAudioRouteItem(audioRoute)

    private fun createCallBottomSheet(bottomSheetLayoutType: BottomSheetLayoutType) {
        currentCallBottomSheetLayoutType = bottomSheetLayoutType
        callBottomSheet?.updateBottomSheetLayoutType(bottomSheetLayoutType)
        callBottomSheet?.let { return }
        callBottomSheet = CallBottomSheet(
            context,
            callActionItems,
            bottomSheetLayoutType,
            context.getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetCallStyle)
        )
        callBottomSheet?.onStateChangedBottomSheetListener = onBottomSheetCallBacks
        callBottomSheet?.onActionBottomSheetListener = onBottomSheetAction
    }

    private fun createRingingBottomSheet(bottomSheetLayoutType: BottomSheetLayoutType) {
        if (bottomSheetLayoutType != ringingBottomSheet?.bottomSheetLayoutType)
            disposeBottomSheet(ringingBottomSheet)
        else return
        ringingBottomSheet = RingingBottomSheet(
            context,
            bottomSheetLayoutType,
            context.getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetRingingStyle)
        )
        ringingBottomSheet?.onStateChangedBottomSheetListener = onBottomSheetCallBacks
        ringingBottomSheet?.onActionBottomSheetListener = onBottomSheetAction
    }

    private fun createAudioRouteBottomSheet(bottomSheetLayoutType: BottomSheetLayoutType) {
        if (bottomSheetLayoutType != audioRouteBottomSheet?.bottomSheetLayoutType)
            disposeBottomSheet(audioRouteBottomSheet)
        else return
        audioRouteBottomSheet = AudioRouteBottomSheet(
            context = context,
            audioRouteItems = onAudioRoutesRequest?.onAudioRoutesRequested(),
            bottomSheetLayoutType = bottomSheetLayoutType,
            bottomSheetStyle = context.getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetAudioRouteStyle),
            onAudioRoutesRequest = onAudioRoutesListener
        )
        audioRouteBottomSheet?.onStateChangedBottomSheetListener = onBottomSheetCallBacks
        audioRouteBottomSheet?.onActionBottomSheetListener = onBottomSheetAction
    }

    fun replaceCallActionItems(items: List<CallAction>) {
        items.forEachIndexed { index, callAction ->
            if (index < items.count() - 1) {
                callAction.itemView?.nextFocusForwardId =
                    items[index + 1].itemView?.id!!
            }
        }
        actions = items
        callBottomSheet?.setItems(callActionItems)
    }

    /**
     * Dispose the widget
     */
    fun dispose() {
        removeAnchorFromAnchoredView()
        onHiddenListener = null
        onShowListener = null
        slidingListener = null
        callBottomSheet?.dispose()
        audioRouteBottomSheet?.dispose()
        ringingBottomSheet?.dispose()
    }

    private fun disposeBottomSheet(bottomSheet: BaseKaleyraBottomSheet?) {
        removeAnchorFromAnchoredView()
        bottomSheet?.dispose()
    }

    /**
     * Click Listener for the call action widget
     */
    interface OnClickListener {
        /**
         * Called when a call action has been requested
         * @param item CallAction requested
         * @param position position of item
         * @return true if has been handled, false otherwise
         */
        fun onCallActionClicked(item: CallAction, position: Int): Boolean

        /**
         * Called when an audioRoute has been selected
         * @param item AudioRoute requested
         * @param position position of item
         * @return true if has been handled, false otherwise
         */
        fun onAudioRouteClicked(item: AudioRoute, position: Int): Boolean
    }

    /**
     * Sliding Listener for the call action widget
     */
    interface SlidingListener {

        /**
         * Called when the widget slide offset changes.
         * @param top distance from the top of the parent in pixels
         * @param isCollapsed true if call action widget is collapsed
         */
        fun onSlide(@Px top: Int, isCollapsed: Boolean)

        /**
         * Called when the call action widget changes state.
         * @param state KaleyraCallActionWidgetState
         */
        fun onStateChanged(state: KaleyraCallActionWidgetState)
    }

    /**
     * Listener to be called after on shown process
     */
    interface OnShowListener {
        /**
         * Callback fired when the widget has been programmatically shown
         */
        fun onShown()
    }

    /**
     * Listener to be called after on hidden process
     */
    interface OnHiddenListener {
        /**
         * Callback fired when the widget has been programmatically hidden
         */
        fun onHidden()
    }

    /**
     * KaleyraCallActionWidget singleton
     */
    companion object {
        /**
         * Default action items grid span size
         */
        const val DEFAULT_ACTION_ITEM_GRID_SPAN_SIZE = 4
    }
}