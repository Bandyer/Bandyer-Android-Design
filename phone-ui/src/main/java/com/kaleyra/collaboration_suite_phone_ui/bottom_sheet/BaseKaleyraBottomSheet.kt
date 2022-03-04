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

package com.kaleyra.collaboration_suite_phone_ui.bottom_sheet

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Space
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.extensions.ColorIntExtensions.requiresLightColor
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.getActivity
import com.kaleyra.collaboration_suite_core_ui.extensions.ContextExtensions.getScreenSize
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingEnd
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.setPaddingStart
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.behaviours.KaleyraBottomSheetBehaviour
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.ActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.items.AdapterActionItem
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.KaleyraBottomSheetLayout
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutContent
import com.kaleyra.collaboration_suite_phone_ui.bottom_sheet.view.BottomSheetLayoutType
import com.kaleyra.collaboration_suite_phone_ui.call.buttons.KaleyraLineButton
import com.kaleyra.collaboration_suite_phone_ui.extensions.checkIsInMultiWindowMode
import com.kaleyra.collaboration_suite_phone_ui.extensions.checkIsInPictureInPictureMode
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCoordinates
import com.kaleyra.collaboration_suite_phone_ui.utils.item_adapter_animators.AlphaCrossFadeAnimator
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.select.SelectExtension
import java.lang.ref.WeakReference
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Base Kaleyra BottomSheet
 * This bottomSheet is using a custom behaviour that enables a third state ( ANCHOR_POINT )
 * The bottomSheet style is composed of a line indicating the top of the sheet a title and a recyclerview
 * @property views List of actions to add
 * @property peekHeight max height of the bottomSheet
 * @property bottomSheetLayoutStyle style of bottomSheet
 * @property bottomSheetLayoutType type of bottomSheet horizontal or vertical
 * @constructor
 * @author kristiyan
 */
open class BaseKaleyraBottomSheet(
    context: AppCompatActivity,
    private var views: List<ActionItem>,
    private val peekHeight: Int?,
    val bottomSheetLayoutType: BottomSheetLayoutType,
    @StyleRes val bottomSheetLayoutStyle: Int
) : KaleyraBottomSheet, SystemViewLayoutObserver {

    private var initialized = false
    private var isAnimating = false
    private var hasMoved = false
    private var wasInMultiWindowMode = false
    private var wasInPictureInPictureMode = false

    /**
     * If animation is enabled
     */
    protected var animationEnabled = false
    private var recyclerViewAlphaDecimalFormat = DecimalFormat("#.##").apply { this.roundingMode = RoundingMode.DOWN }
    private var valueAnimator: ValueAnimator? = null

    private fun getKaleyraBottomSheetLayout(parent: View?): KaleyraBottomSheetLayout? {
        if (parent !is ViewGroup) return null
        if (parent is KaleyraBottomSheetLayout) return parent

        val count = parent.childCount
        var child: KaleyraBottomSheetLayout? = null

        var i = 0
        while (child == null && i < count) {
            child = getKaleyraBottomSheetLayout(parent.getChildAt(i))
            i++
        }

        return child
    }

    private var slideOffset = -1f

    private val bottomSheetBehaviorCallback = object : KaleyraBottomSheetBehaviour.BottomSheetCallback() {

        override fun onDrawn(bottomSheet: View, state: Int, slideOffset: Float) {
            slideAnimationReady(this@BaseKaleyraBottomSheet, state, slideOffset)
        }

        @SuppressLint("SwitchIntDef")
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState != KaleyraBottomSheetBehaviour.STATE_HIDDEN) {
                bottomSheetLayoutContent.updateBackgroundView()
                moveBottomSheet()
            }
            when (newState) {
                KaleyraBottomSheetBehaviour.STATE_HIDDEN       -> onHidden()
                KaleyraBottomSheetBehaviour.STATE_COLLAPSED    -> onCollapsed()
                KaleyraBottomSheetBehaviour.STATE_EXPANDED     -> onExpanded()
                KaleyraBottomSheetBehaviour.STATE_SETTLING     -> onSettling()
                KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT -> onAnchor()
                KaleyraBottomSheetBehaviour.STATE_DRAGGING     -> onDragging()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            this@BaseKaleyraBottomSheet.slideOffset = slideOffset
            if ((slideOffset == 0f && bottomSheetBehaviour?.skipCollapsed == false) &&
                (bottomSheetBehaviour?.lastStableState == KaleyraBottomSheetBehaviour.STATE_COLLAPSED ||
                        bottomSheetBehaviour?.lastStableState == KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT)
            ) return
            onStateChangedBottomSheetListener?.onSlide(this@BaseKaleyraBottomSheet, slideOffset)
            bottomSheetLayoutContent.updateBackgroundView()
            slideAnimationUpdate(this@BaseKaleyraBottomSheet, slideOffset)
        }
    }

    /**
     * Context
     */
    protected val mContext = WeakReference(context)

    /**
     * Behaviour applied
     */
    var bottomSheetBehaviour: KaleyraBottomSheetBehaviour<View>? = null

    /**
     * RecyclerView adapter
     */
    var fastItemAdapter: ItemAdapter<AdapterActionItem> = ItemAdapter()

    /**
     * Fast adapter
     */
    var fastAdapter = FastAdapter.with(fastItemAdapter)

    /**
     * Layout Content
     */
    var bottomSheetLayoutContent: BottomSheetLayoutContent = BottomSheetLayoutContent(ContextThemeWrapper(context, bottomSheetLayoutStyle))

    private var coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout? = null
    private var currentCoordinatorLayoutHeight = -1

    override var state = -1
        get() = bottomSheetBehaviour?.state ?: -1

    final override var recyclerView = bottomSheetLayoutContent.recyclerView
    final override var lineView: KaleyraLineButton? = bottomSheetLayoutContent.lineView
    final override var titleView: MaterialTextView? = bottomSheetLayoutContent.titleView

    /**
     * Height of bottom navigation
     */
    var bottomMarginNavigation = -1

    override var onStateChangedBottomSheetListener: OnStateChangedBottomSheetListener<KaleyraBottomSheet>? = null

    /**
     * Called to restore the current state of the bottomSheet
     * @param savedInstanceState Bundle? data previously saved
     * @param prefix String keyName to restore
     */
    protected open fun onRestoreInstanceState(savedInstanceState: Bundle?, prefix: String) {
        savedInstanceState ?: return
        fastAdapter.withSavedInstanceState(savedInstanceState, prefix)
    }

    /**
     * Called to save the current state of the bottomSheet
     * @param bundle Bundle? data to save
     * @param prefix String give a keyName
     * @return Bundle? the new bundle
     */
    protected open fun onSaveInstanceState(bundle: Bundle?, prefix: String): Bundle? {
        bundle ?: return bundle
        return fastAdapter.saveInstanceState(bundle, prefix)
    }

    /**
     * Called when the slideAnimation has been updated
     * @param bottomSheet KaleyraBottomSheet?
     * @param slideOffset Float
     */
    protected open fun slideAnimationUpdate(bottomSheet: KaleyraBottomSheet?, slideOffset: Float) {
        if ((slideOffset >= 0f && hasMoved && !isAnimating) || slideOffset < 0f) fadeRecyclerViewLinesBelowNavigation()
        when {
            slideOffset <= 0f -> updateNavigationBar(false)
            else              -> updateNavigationBar()
        }
    }

    /**
     *
     * @param bottomSheet KaleyraBottomSheet?
     * @param state Int
     * @param slideOffset Float
     */
    protected open fun slideAnimationReady(bottomSheet: KaleyraBottomSheet?, state: Int, slideOffset: Float) = Unit

    private fun configureBottomSheet(bottomSheetLayoutContent: BottomSheetLayoutContent) {
        val context = bottomSheetLayoutContent.context.getActivity<Activity>() ?: return
        coordinatorLayout = getKaleyraBottomSheetLayout(context.window.decorView as ViewGroup)
        if (coordinatorLayout == null)
            throw RuntimeException("Please add a KaleyraBottomSheetLayout in your layout, where you want to display the bottomSheet!!")

        initLayout()
    }

    private fun initLayout() {
        if (initialized)
            return

        coordinatorLayout!!.setOnTouchListener { v, event ->
            if (recyclerView!!.canScrollVertically(-1)) {
                recyclerView!!.onTouchEvent(event)
                true
            } else false
        }

        coordinatorLayout!!.addView(bottomSheetLayoutContent, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addBackground()
        val params = bottomSheetLayoutContent.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        params.behavior = KaleyraBottomSheetBehaviour<View>(bottomSheetLayoutContent.context, null)
        bottomSheetLayoutContent.layoutParams = params
        bottomSheetBehaviour = KaleyraBottomSheetBehaviour.from(bottomSheetLayoutContent)
        bottomSheetBehaviour!!.addBottomSheetCallback(bottomSheetBehaviorCallback)

        peekHeight?.let { bottomSheetBehaviour!!.peekHeight = it }

        bottomSheetLayoutContent.setOnKeyListener { v, keyCode, event ->
            if (event.action != KeyEvent.ACTION_UP || event.keyCode != KeyEvent.KEYCODE_BACK)
                return@setOnKeyListener false

            val behaviour = bottomSheetBehaviour ?: run {
                return@setOnKeyListener false
            }

            when (behaviour.state) {
                KaleyraBottomSheetBehaviour.STATE_EXPANDED     -> {
                    behaviour.state = when {
                        !behaviour.skipCollapsed -> KaleyraBottomSheetBehaviour.STATE_COLLAPSED
                        behaviour.isHideable     -> KaleyraBottomSheetBehaviour.STATE_HIDDEN
                        !behaviour.skipAnchor    -> KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT
                        else                     -> return@setOnKeyListener false
                    }
                    true
                }
                KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT -> {
                    behaviour.state = when {
                        !behaviour.skipCollapsed -> KaleyraBottomSheetBehaviour.STATE_COLLAPSED
                        behaviour.isHideable     -> KaleyraBottomSheetBehaviour.STATE_HIDDEN
                        else                     -> return@setOnKeyListener false
                    }
                    true
                }
                KaleyraBottomSheetBehaviour.STATE_COLLAPSED    -> {
                    if (behaviour.isHideable)
                        behaviour.state = KaleyraBottomSheetBehaviour.STATE_HIDDEN
                    return@setOnKeyListener behaviour.isHideable
                }
                else                                           -> false
            }
        }
        initialized = true
    }

    /**
     * On configuration changed
     */
    open fun onConfigurationChanged() = Unit

    private fun addBackground() {
        val parent = coordinatorLayout?.parent as? ViewGroup
        bottomSheetLayoutContent.backgroundView?.tag = this::class.java.name
        when (parent) {
            is RelativeLayout, is FrameLayout, is androidx.constraintlayout.widget.ConstraintLayout, is androidx.coordinatorlayout.widget.CoordinatorLayout -> {
                val position = (parent.indexOfChild(coordinatorLayout).takeIf { it > 0 } ?: 1)
                bottomSheetLayoutContent.post {
                    bottomSheetLayoutContent.navigationBarHeight = bottomMarginNavigation

                    with(bottomSheetLayoutContent.backgroundView) {
                        if (this?.parent != null) {
                            (this.parent as? ViewGroup)?.removeView(this)
                        }

                        val height = bottomSheetLayoutContent.height + bottomSheetLayoutContent.paddingTop + bottomSheetLayoutContent.paddingBottom + bottomMarginNavigation

                        val view = Space(this?.context)
                        this?.addView(view, 0, ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height))
                        parent.addView(
                            this,
                            position,
                            ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        )
                    }

                }
            }
        }
    }

    private fun getBackground(): View? {
        return (coordinatorLayout?.parent as? ViewGroup)?.findViewWithTag(this::class.java.name)
    }

    /**
     * Returns true if is currently expanded, false otherwise.
     * @return Boolean
     */
    fun isExpanded(): Boolean = state == KaleyraBottomSheetBehaviour.STATE_EXPANDED

    /**
     * Returns true if is currently anchored, false otherwise.
     * @return Boolean
     */
    fun isAnchored(): Boolean = state == KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT

    /**
     * Returns true if is currently collapsed, false otherwise.
     * @return Boolean
     */
    fun isCollapsed(): Boolean = state == KaleyraBottomSheetBehaviour.STATE_COLLAPSED

    /**
     * Called onCollapsed bottomSheet
     */
    open fun onCollapsed() {
        onStateChangedBottomSheetListener?.onCollapse(this)
        bottomSheetBehaviour ?: return
        if (!hasMoved) {
            moveBottomSheet()
            hasMoved = true
        }
        lineView?.state = KaleyraLineButton.State.COLLAPSED

        updateNavigationBar(false)

        fadeRecyclerViewLinesBelowNavigation()
    }

    /**
     * Called onExpanded bottomSheet
     */
    open fun onExpanded() {
        onStateChangedBottomSheetListener?.onExpand(this)
        if (!hasMoved) {
            moveBottomSheet()
            hasMoved = true
        }
        updateNavigationBar()
    }

    /**
     * Called onHidden bottomSheet
     */
    open fun onHidden() = onStateChangedBottomSheetListener?.onHide(this)

    /**
     * Called onSettling bottomSheet
     */
    open fun onSettling() {
    }

    /**
     * Called onDragging bottomSheet
     */
    open fun onDragging() {
    }

    /**
     * Called onAnchor bottomSheet
     */
    open fun onAnchor() {
        onStateChangedBottomSheetListener?.onAnchor(this)
        if (!hasMoved) {
            moveBottomSheet()
            hasMoved = true
        }
        fadeRecyclerViewLinesBelowNavigation()
        updateNavigationBar()
    }

    private fun updateNavigationBar(isLightNavigationBar: Boolean? = null) {
        val window = coordinatorLayout!!.context.getActivity<AppCompatActivity>()?.window ?: return
        val cardViewBackgroundColor = bottomSheetLayoutContent.backgroundView?.cardBackgroundColor?.defaultColor ?: return

        WindowInsetsControllerCompat(window, coordinatorLayout!!.rootView).isAppearanceLightNavigationBars = isLightNavigationBar ?: !cardViewBackgroundColor.requiresLightColor()
    }

    /**
     * Fade animation to recycler view rows if below navigation bar
     */
    fun fadeRecyclerViewLinesBelowNavigation(fade: Boolean? = null) {
        val decorView = mContext.get()?.window?.decorView ?: return
        decorView.post {
            if (!animationEnabled) return@post
            val screenHeight = bottomSheetLayoutContent.context.getScreenSize().y
            val navigationLimit = if (fade == null) (screenHeight - bottomMarginNavigation) else 0
            val hasNavigationBar = bottomMarginNavigation > 0
            val canShowFirstRowWhenCollapsed = bottomSheetBehaviour?.skipCollapsed == true || (bottomSheetBehaviour?.skipCollapsed == false && bottomSheetBehaviour!!.lastStableState != KaleyraBottomSheetBehaviour.STATE_COLLAPSED)
            views.forEachIndexed { index, itemView ->
                val view = itemView.itemView ?: return@forEachIndexed
                val canShowSecondRowWhenAnchored = isAnimating && !recyclerView.isFirstRow(index) && bottomSheetBehaviour?.lastStableState == KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT

                when {
                    canShowSecondRowWhenAnchored -> {
                        view.alpha = 0f
                    }
                    (!hasMoved || (isAnimating && (if (!bottomSheetBehaviour!!.skipCollapsed) slideOffset > 0.1f else slideOffset >= 0.1f))) && recyclerView.isFirstRow(index) || (!hasNavigationBar && isVisible() && canShowFirstRowWhenCollapsed) -> {
                        view.alpha = 1f
                    }
                    fade == null -> {
                        val viewBottom = view.getCoordinates().y + view.height + (screenHeight - decorView.height)
                        val hidden = viewBottom - navigationLimit
                        view.alpha = if (!hasNavigationBar) 1f else (1 - hidden / view.height.toFloat()).takeIf { it > 0.23 }?.coerceAtMost(1f)?.apply {
                            recyclerViewAlphaDecimalFormat.format(this)
                        } ?: 0f
                    }
                    else -> view.alpha = if (fade) 1f else 0f
                }
            }
        }
    }

    private fun RecyclerView?.isFirstRow(position: Int) = when (val manager = this?.layoutManager) {
        is GridLayoutManager -> position < manager.spanCount
        else                 -> position == 0
    }

    override fun isVisible() = bottomSheetLayoutContent.visibility == View.VISIBLE && initialized

    init {
        when (bottomSheetLayoutType) {
            is BottomSheetLayoutType.GRID -> {
                recyclerView!!.layoutManager =
                    GridLayoutManager(
                        recyclerView!!.context,
                        bottomSheetLayoutType.spanSize.takeIf { views.size >= it} ?: views.size,
                        if (bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                        false)
            }
            is BottomSheetLayoutType.LIST -> {
                recyclerView!!.layoutManager =
                    LinearLayoutManager(
                        recyclerView!!.context,
                        if (bottomSheetLayoutType.orientation == BottomSheetLayoutType.Orientation.HORIZONTAL) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                        false)
            }
        }
        recyclerView!!.adapter = fastAdapter
        recyclerView!!.itemAnimator = AlphaCrossFadeAnimator()
    }

    final override fun onTopInsetChanged(pixels: Int) = Unit

    final override fun onBottomInsetChanged(pixels: Int) {
        val activity = mContext.get() ?: return

        val screenHeight = activity.getScreenSize().y
        val guessKeyboardShown = screenHeight > 0 && pixels > screenHeight * 0.15f
        if (guessKeyboardShown) return

        val isInPictureInPictureMode = activity.checkIsInPictureInPictureMode()
        val isInMultiWindowMode = activity.checkIsInMultiWindowMode() && !isInPictureInPictureMode

        if (isInMultiWindowMode && !isInPictureInPictureMode || (wasInMultiWindowMode && !wasInPictureInPictureMode)) onConfigurationChanged()

        if (!isInMultiWindowMode && wasInMultiWindowMode) wasInMultiWindowMode = false
        if (!isInPictureInPictureMode && wasInPictureInPictureMode) wasInPictureInPictureMode = false

        bottomMarginNavigation = pixels
        bottomSheetLayoutContent.navigationBarHeight = pixels
        moveBottomSheet()
    }

    /**
     * Move bottom sheet
     *
     */
    protected fun moveBottomSheet() {
        if (isAnimating) valueAnimator?.cancel()
        if (state == -1) return

        coordinatorLayout ?: return

        val lp = coordinatorLayout!!.layoutParams as ViewGroup.MarginLayoutParams
        val bottomMargin = lp.bottomMargin

        val startValue = bottomMargin.toFloat()
        val endValue = bottomMarginNavigation.toFloat()
        val isStable = startValue == endValue

        isAnimating = !isStable

        valueAnimator?.removeAllListeners()
        valueAnimator?.cancel()

        valueAnimator = ValueAnimator.ofFloat(startValue, endValue)
        valueAnimator!!.interpolator = DecelerateInterpolator()

        valueAnimator!!.addUpdateListener {
            kotlin.runCatching {
                val value = it.animatedValue as Float
                lp.bottomMargin = value.toInt()
                coordinatorLayout?.requestLayout()
                bottomSheetLayoutContent.updateBackgroundView()
                if (bottomMarginNavigation > 0 && !isStable) fadeRecyclerViewLinesBelowNavigation()
                onStateChangedBottomSheetListener?.onSlide(this@BaseKaleyraBottomSheet, bottomSheetLayoutContent.top.toFloat())
            }
        }

        valueAnimator?.addListener(
            object : Animator.AnimatorListener {
                var isCanceled = false
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    if (endValue != 0f) fadeRecyclerViewLinesBelowNavigation()
                    isAnimating = false
                    if (isCanceled) return
                    bottomSheetLayoutContent.updateBackgroundView()
                    if (endValue != lp.bottomMargin.toFloat()) moveBottomSheet()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isCanceled = true
                    isAnimating = false
                }

                override fun onAnimationStart(animation: Animator?) = Unit
            })

        valueAnimator!!.duration = 200
        valueAnimator!!.start()
    }

    override fun dispose() {
        dismiss()
        bottomSheetBehaviour?.removeBottomSheetCallback(bottomSheetBehaviorCallback)
        SystemViewLayoutOffsetListener.removeObserver(mContext.get()!!, this)
    }

    override fun onRightInsetChanged(pixels: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return

        bottomSheetLayoutContent.post {
            bottomSheetLayoutContent.setPaddingEnd(pixels)
        }
    }

    override fun onLeftInsetChanged(pixels: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return

        bottomSheetLayoutContent.post {
            bottomSheetLayoutContent.setPaddingStart(pixels)
        }
    }

    override fun show() {
        bottomSheetLayoutContent.visibility = View.VISIBLE

        bottomSheetLayoutContent.post {
            SystemViewLayoutOffsetListener.getOffsets(mContext.get()!!)
        }

        SystemViewLayoutOffsetListener.addObserver(mContext.get()!!, this)

        if (!initialized) setItems(views)
        configureBottomSheet(bottomSheetLayoutContent)

        onStateChangedBottomSheetListener?.onShow(this)
    }

    private fun dismiss() {
        valueAnimator?.cancel()

        bottomSheetLayoutContent.backgroundView?.parent?.let { (it as ViewGroup).removeView(bottomSheetLayoutContent.backgroundView) }
        if (!initialized) return

        coordinatorLayout?.removeView(bottomSheetLayoutContent)
        bottomSheetBehaviour = null
        initialized = false
    }

    override fun anchor() {
        lineView!!.visibility = if (bottomSheetBehaviour!!.disableDragging) View.GONE else View.VISIBLE
        bottomSheetLayoutContent.post {
            val behaviour = bottomSheetBehaviour ?: return@post
            behaviour.skipAnchor = false
            behaviour.state = KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT
        }
        updateNavigationBar()
    }

    override fun expand() {
        lineView!!.visibility = if (bottomSheetBehaviour!!.disableDragging) View.GONE else View.VISIBLE
        bottomSheetLayoutContent.post {
            bottomSheetBehaviour?.state = KaleyraBottomSheetBehaviour.STATE_EXPANDED
        }
        updateNavigationBar()
    }

    override fun collapse(to: View?, offset: Int?) {
        bottomSheetLayoutContent.post {
            val behaviour = bottomSheetBehaviour ?: return@post
            val peekHeight = when (to) {
                null -> 0
                else -> to.measuredHeight
            }
            behaviour.peekHeight = peekHeight + (offset ?: 0)
            behaviour.skipCollapsed = false
            behaviour.state = KaleyraBottomSheetBehaviour.STATE_COLLAPSED
        }
        updateNavigationBar()
    }

    override fun toggle() {
        bottomSheetBehaviour?.let {
            when (it.state) {
                KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT -> it.state = if (it.isHideable) KaleyraBottomSheetBehaviour.STATE_HIDDEN else if (!it.skipCollapsed) KaleyraBottomSheetBehaviour.STATE_COLLAPSED else KaleyraBottomSheetBehaviour.STATE_EXPANDED
                KaleyraBottomSheetBehaviour.STATE_EXPANDED     -> it.state = if (it.isHideable) KaleyraBottomSheetBehaviour.STATE_HIDDEN else if (!it.skipCollapsed) KaleyraBottomSheetBehaviour.STATE_COLLAPSED else if (!it.skipAnchor) KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT else KaleyraBottomSheetBehaviour.STATE_EXPANDED
                KaleyraBottomSheetBehaviour.STATE_COLLAPSED    -> it.state = if (!it.skipAnchor) KaleyraBottomSheetBehaviour.STATE_ANCHOR_POINT else KaleyraBottomSheetBehaviour.STATE_EXPANDED
            }
        }
    }

    override fun hide(force: Boolean) {
        bottomSheetBehaviour ?: return
        if (bottomSheetBehaviour!!.state == KaleyraBottomSheetBehaviour.STATE_HIDDEN)
            return

        bottomSheetLayoutContent.visibility = View.INVISIBLE
        
        if (bottomSheetBehaviour!!.isHideable) {
            bottomSheetBehaviour!!.state = KaleyraBottomSheetBehaviour.STATE_HIDDEN
        } else {
            if (force) {
                bottomSheetBehaviour!!.isHideable = true
                bottomSheetBehaviour!!.state = KaleyraBottomSheetBehaviour.STATE_HIDDEN
                return
            }
            bottomSheetBehaviour!!.state = KaleyraBottomSheetBehaviour.STATE_COLLAPSED
        }
    }

    override fun setItems(items: List<ActionItem>) {
        this.views = items
        val diffResult = FastAdapterDiffUtil.calculateDiff(fastItemAdapter, items.map { AdapterActionItem(it) })
        FastAdapterDiffUtil[fastItemAdapter] = diffResult
    }

    override fun getItem(position: Int): ActionItem? {
        return fastItemAdapter.getAdapterItem(position).item
    }

    override fun getItemIndex(item: ActionItem): Int {
        return fastItemAdapter.adapterItems.indexOfFirst { it.item == item }
    }

    override fun removeItem(item: ActionItem) {
        val position = fastItemAdapter.adapterItems.indexOfFirst { it.item == item }
        if (position != -1) {
            fastAdapter.getExtension<SelectExtension<AdapterActionItem>>(SelectExtension::class.java)?.deselect(position)
            fastItemAdapter.remove(position)
        }
    }

    override fun addItem(item: ActionItem, position: Int) {
        val bottomSheetItem = AdapterActionItem(item)
        if (bottomSheetItem !in fastItemAdapter.adapterItems)
            fastItemAdapter.add(position, bottomSheetItem)
    }

    override fun updateItem(item: ActionItem) {
        fastItemAdapter.adapterItems.firstOrNull { it.item == item }?.let {
            fastItemAdapter.adapterItems.indexOf(it).takeIf { it != -1 }?.let { index ->
                fastItemAdapter.adapterItems[index] = AdapterActionItem(item)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun <T : ActionItem> firstOrNull(actionItem: Class<T>): T? {
        return fastItemAdapter.adapterItems.firstOrNull { it.item.javaClass.name.contains(actionItem.name) }?.item as T?
    }

    override fun setItem(item: ActionItem, position: Int) {
        if (position < 0)
            return
        if (item.viewLayoutRes != fastItemAdapter.getAdapterItem(position).item.viewLayoutRes)
            fastItemAdapter[position] = AdapterActionItem(item)
    }

    override fun replaceItems(old: ActionItem, new: ActionItem) {
        setItem(new, getItemIndex(old))
    }
}