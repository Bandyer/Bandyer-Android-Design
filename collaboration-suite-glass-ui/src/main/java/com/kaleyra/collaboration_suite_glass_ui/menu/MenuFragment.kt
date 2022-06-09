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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ChangeVolume
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ChangeZoom
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.OpenChat
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.OpenWhiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ShowParticipants
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.SwitchCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleCamera
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleFlashlight
import com.kaleyra.collaboration_suite_core_ui.CallUI.Action.ToggleMicrophone
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
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
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * KaleyraGlassMenuFragment
 */
internal class MenuFragment : BaseFragment<GlassCallActivity>(), TiltListener {

    private var _binding: KaleyraGlassFragmentMenuBinding? = null
    override val binding: KaleyraGlassFragmentMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<MenuItem>? = null

    private var menuItems = listOf<MenuItem>()
    private val hiddenActions = mutableSetOf<CallAction>()

    private var mutex = Mutex()

    private val args by lazy { MenuFragmentArgs.fromBundle(requireActivity().intent.extras!!) }

    private var currentMenuItemIndex = 0

    private val viewModel: CallViewModel by activityViewModels()

    private var chatUI: ChatUI? = null

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
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
        // Close the menu if the actions change
        viewModel.actions.drop(1).take(1).onEach { onSwipeDown() }.launchIn(lifecycleScope)

        val actions = getActions(viewModel.actions.value)
        menuItems = actions.map { MenuItem(it) }.also { itemAdapter!!.add(it) }

        val cameraAction = actions.filterIsInstance<CallAction.CAMERA>().firstOrNull()
        val micAction = actions.filterIsInstance<CallAction.MICROPHONE>().firstOrNull()
        val zoomAction = actions.filterIsInstance<CallAction.ZOOM>().firstOrNull()
        val flashAction = actions.filterIsInstance<CallAction.FLASHLIGHT>().firstOrNull()
        val switchCameraAction = actions.filterIsInstance<CallAction.SWITCHCAMERA>().firstOrNull()

        repeatOnStarted {
            cameraAction?.also { action ->
                viewModel.cameraEnabled.onEach { action.toggle(it) }.launchIn(this)
                viewModel.camPermission.onEach { action.disable(!it.isAllowed && it.neverAskAgain) }
                    .launchIn(this)
            }
            micAction?.also { action ->
                viewModel.micEnabled.onEach { action.toggle(it) }.launchIn(this)
                viewModel.micPermission.onEach { action.disable(!it.isAllowed && it.neverAskAgain) }
                    .launchIn(this)
            }
            zoomAction?.also { action ->
                combine(viewModel.cameraEnabled, viewModel.zoom) { cameraEnabled, zoom ->
                    updateMenuItems(!cameraEnabled || zoom == null, action)
                }.launchIn(this)
            }
            flashAction?.also { action ->
                combine(
                    viewModel.cameraEnabled,
                    viewModel.flashLight
                ) { cameraEnabled, flashLight ->
                    updateMenuItems(!cameraEnabled || flashLight == null, action)
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
            }
            switchCameraAction?.also { action ->
                combine(
                    viewModel.cameraEnabled,
                    viewModel.hasSwitchCamera
                ) { cameraEnabled, hasSwitchCamera ->
                    updateMenuItems(!(cameraEnabled && hasSwitchCamera), action)
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

    private suspend fun updateMenuItems(hide: Boolean, action: CallAction) {
        mutex.withLock {
            hiddenActions.apply { if (hide) add(action) else remove(action) }
            val diff = menuItems.filterNot { hiddenActions.contains(it.action) }
            FastAdapterDiffUtil[itemAdapter!!] =
                FastAdapterDiffUtil.calculateDiff(itemAdapter!!, diff, true)
        }
    }

    private fun getActions(actions: Set<Action>): List<CallAction> = CallAction.getActions(
        requireContext(),
        withMicrophone = actions.any { it is ToggleMicrophone },
        withCamera = actions.any { it is ToggleCamera },
        withSwitchCamera = actions.any { it is SwitchCamera },
        withFlashLight = actions.any { it is ToggleFlashlight },
        withVolume = actions.any { it is ChangeVolume },
        withZoom = actions.any { it is ChangeZoom },
        withParticipants = actions.any { it is ShowParticipants },
        withChat = actions.any { it is OpenChat },
        withWhiteboard = actions.any { it is OpenWhiteboard }
    )

    override fun onTap() = onTap(itemAdapter!!.getAdapterItem(currentMenuItemIndex).action)

    private fun onTap(action: CallAction) = when (action) {
        is CallAction.MICROPHONE -> true.also {
            if (!viewModel.micPermission.value.isAllowed) viewModel.onRequestMicPermission(
                requireActivity()
            )
            viewModel.onEnableMic(!viewModel.micEnabled.value)
        }
        is CallAction.CAMERA -> true.also {
            if (!viewModel.camPermission.value.isAllowed) viewModel.onRequestCameraPermission(
                requireActivity()
            )
            viewModel.onEnableCamera(!viewModel.cameraEnabled.value)
        }

        is CallAction.SWITCHCAMERA -> true.also { viewModel.onSwitchCamera() }
        is CallAction.VOLUME -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToVolumeFragment()) }
        is CallAction.ZOOM -> true.also {
            findNavController().safeNavigate(
                MenuFragmentDirections.actionMenuFragmentToZoomFragment()
            )
        }
        is CallAction.FLASHLIGHT -> true.also {
            if (action.isDisabled) return@also
            val flashLight = viewModel.flashLight.value ?: return@also
            val isEnabled = flashLight.enabled.value
            if (isEnabled) flashLight.tryDisable() else flashLight.tryEnable()
        }
        is CallAction.PARTICIPANTS -> true.also {
            findNavController().safeNavigate(
                MenuFragmentDirections.actionMenuFragmentToParticipantsFragment()
            )
        }
        is CallAction.CHAT -> true.also {
            chatUI = chatUI ?: CollaborationUI.chatBox.create(viewModel.participants.replayCache.first().others)
            CollaborationUI.chatBox.show(requireActivity(), chatUI!!)
        }
        is CallAction.WHITEBOARD -> true.also {
            onSwipeDown()
            (requireActivity() as GlassCallActivity).rvStreams.smoothScrollToPosition(0)
        }
        else -> false
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMenu.horizontalSmoothScrollToNext(currentMenuItemIndex)
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMenu.horizontalSmoothScrollToPrevious(currentMenuItemIndex)
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraMenu.scrollBy(
            (deltaAzimuth * requireContext().tiltScrollFactor()).toInt(),
            0
        )

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true) })
    }
}