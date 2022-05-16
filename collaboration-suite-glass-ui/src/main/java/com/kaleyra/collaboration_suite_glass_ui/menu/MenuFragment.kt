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

package com.kaleyra.collaboration_suite_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_core_ui.model.Option
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.call.CallAction
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.call.GlassCallActivity
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.MenuProgressIndicator
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentMenuBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * KaleyraGlassMenuFragment
 */
internal class MenuFragment : BaseFragment<GlassCallActivity>(), TiltListener {

    private var _binding: KaleyraGlassFragmentMenuBinding? = null
    override val binding: KaleyraGlassFragmentMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<MenuItem>? = null

    private val args: MenuFragmentArgs by lazy { MenuFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private var currentMenuItemIndex = 0

    private val viewModel: CallViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Add view binding
        _binding = KaleyraGlassFragmentMenuBinding
            .inflate(inflater, container, false)
            .apply {
                if (DeviceUtils.isRealWear)
                    setListenersForRealWear(kaleyraBottomNavigation)

                // Init the RecyclerView
                with(kaleyraMenu) {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!).apply {
                        onClickListener = { _, _, item, _ -> onTap(item.action); false }
                    }
                    val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    setHasFixedSize(true)

                    addItemDecoration(HorizontalCenterItemDecoration())
                    addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
                            currentMenuItemIndex = layoutManager.getPosition(foundView)
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> this.onTouchEvent(event) }
                }
            }

        return binding.root
    }

    override fun onServiceBound() {
        val hasAudio = viewModel.preferredCallType.hasAudio()
        val hasVideo = viewModel.preferredCallType.hasVideo()
        val options = args.options ?: arrayOf()
        getActions(hasAudio, hasVideo, options).forEach { itemAdapter!!.add(MenuItem(it)) }

        val cameraAction = (itemAdapter!!.adapterItems.firstOrNull { it.action is CallAction.CAMERA }?.action as? CallAction.ToggleableCallAction)
        val micAction = (itemAdapter!!.adapterItems.firstOrNull { it.action is CallAction.MICROPHONE }?.action as? CallAction.ToggleableCallAction)
        val zoomAction = (itemAdapter!!.adapterItems.firstOrNull { it.action is CallAction.ZOOM }?.action as? CallAction.ToggleableCallAction)
        val flashAction = (itemAdapter!!.adapterItems.firstOrNull { it.action is CallAction.FLASHLIGHT }?.action as? CallAction.ToggleableCallAction)
        val switchCameraAction = (itemAdapter!!.adapterItems.firstOrNull { it.action is CallAction.SWITCHCAMERA }?.action as? CallAction.ToggleableCallAction)

        repeatOnStarted {
            cameraAction?.also { action ->
               viewModel.cameraEnabled.onEach { action.toggle(it) }.launchIn(this)
                viewModel.camPermission.onEach { action.disable(!it.isAllowed && it.neverAskAgain) }.launchIn(this)
            }
            micAction?.also { action ->
                viewModel.micEnabled.onEach { action.toggle(it) }.launchIn(this)
                viewModel.micPermission.onEach { action.disable(!it.isAllowed && it.neverAskAgain) }.launchIn(this)
            }
            zoomAction?.also { action ->
                combine(viewModel.cameraEnabled, viewModel.zoom) { cameraEnabled, zoom ->
                    action.disable(!cameraEnabled || zoom == null)
                }.launchIn(this)
            }
            flashAction?.also { action ->
                combine(viewModel.cameraEnabled, viewModel.flashLight) { cameraEnabled, flashLight ->
                    action.disable(!cameraEnabled || flashLight == null)
                }.launchIn(this)
                viewModel.cameraEnabled
                    .onEach {
                        if (it) return@onEach
                        viewModel.flashLight.value?.tryDisable()
                    }
                    .launchIn(this)
                viewModel.flashLight
                    .filter { it != null }
                    .flatMapLatest { it!!.enabled }
                    .onEach { action.toggle(!it) }
                    .launchIn(this)
//                viewModel.hasFlashLight
//                    .onEach {
//                        Log.e("MenuFragment", "$it")
//                        action.binding?.root?.visibility = if (!it) View.GONE else View.VISIBLE
//                    }
//                    .launchIn(this)
            }
            switchCameraAction?.also { action ->
                combine(viewModel.cameraEnabled, viewModel.hasSwitchCamera) { cameraEnabled, hasSwitchCamera ->
                    action.disable(!cameraEnabled || !hasSwitchCamera)
                }.launchIn(this)
            }
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    private fun getActions(hasAudio: Boolean, hasVideo: Boolean, options: Array<Option>): List<CallAction> {
        var withChat = false

        options.forEach {
            when (it) {
                is Option.CHAT -> withChat = true
                else           -> Unit
            }
        }

        return CallAction.getActions(requireContext(), withMicrophone = hasAudio, withCamera = hasVideo, withSwitchCamera = hasVideo, withFlashLight = hasVideo, withZoom = hasVideo, withChat = true)
    }

//    override fun onDismiss() = Unit

    override fun onTap() = onTap(itemAdapter!!.getAdapterItem(currentMenuItemIndex).action)

    private fun onTap(action: CallAction) = when (action) {
        is CallAction.MICROPHONE   -> true.also {
            if (!viewModel.micPermission.value.isAllowed) viewModel.onRequestMicPermission(requireActivity())
            viewModel.onEnableMic(!viewModel.micEnabled.value)
        }
        is CallAction.CAMERA       -> true.also {
            if (!viewModel.camPermission.value.isAllowed) viewModel.onRequestCameraPermission(requireActivity())
            viewModel.onEnableCamera(!viewModel.cameraEnabled.value)
        }

        is CallAction.SWITCHCAMERA -> true.also { viewModel.onSwitchCamera() }
        is CallAction.VOLUME       -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToVolumeFragment()) }
        is CallAction.ZOOM         -> true.also { if (!action.isDisabled) findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToZoomFragment()) }
        is CallAction.FLASHLIGHT   -> true.also {
            if (action.isDisabled) return@also
            val flashLight = viewModel.flashLight.value ?: return@also
            val isEnabled = flashLight.enabled.value
            if (isEnabled) flashLight.tryDisable() else flashLight.tryEnable()
        }
        is CallAction.PARTICIPANTS -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToParticipantsFragment()) }
        is CallAction.CHAT         -> true.also { CollaborationUI.chatBox.show(CollaborationUI.chatBox.create(viewModel.participants.replayCache.first().others)) }
        else                       -> false
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if (it) binding.kaleyraMenu.horizontalSmoothScrollToNext(currentMenuItemIndex) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if (it) binding.kaleyraMenu.horizontalSmoothScrollToPrevious(currentMenuItemIndex) }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraMenu.scrollBy((deltaAzimuth * requireContext().tiltScrollFactor()).toInt(), 0)

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true)  })
    }
}