package com.bandyer.video_android_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.call.CallAction
import com.bandyer.video_android_glass_ui.call.EmptyFragmentArgs
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentMenuBinding
import com.bandyer.video_android_glass_ui.model.Option
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * BandyerGlassMenuFragment
 */
internal class MenuFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentMenuBinding? = null
    override val binding: BandyerGlassFragmentMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<MenuItem>? = null

    private val args: MenuFragmentArgs by lazy { MenuFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private var currentMenuItemIndex = 0

    private val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

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
        _binding = BandyerGlassFragmentMenuBinding
            .inflate(inflater, container, false)
            .apply {
                if (GlassDeviceUtils.isRealWear)
                    bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                with(bandyerMenu) {
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

        getActions(args.options!!).forEach { itemAdapter!!.add(MenuItem(it)) }

        val cameraAction = (itemAdapter!!.adapterItems.first { it.action is CallAction.CAMERA }.action as CallAction.ToggleableCallAction)
        val micAction = (itemAdapter!!.adapterItems.first { it.action is CallAction.MICROPHONE }.action as CallAction.ToggleableCallAction)

        repeatOnStarted {
            viewModel.cameraEnabled.onEach { cameraAction.toggle(it) }.launchIn(this)
            viewModel.micEnabled.onEach { micAction.toggle(it) }.launchIn(this)
            viewModel.permissions.onEach {
                    micAction.disable(!it.microphoneAllowed && it.nOfRetries < 1)
                    cameraAction.disable(!it.deviceCameraAllowed && it.nOfRetries < 1)
                }.launchIn(this)
        }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    private fun getActions(options: Array<Option>): List<CallAction> {
        var micToggled: Boolean? = null
        var cameraToggled: Boolean? = null
        var withZoom = false
        var withParticipants = false
        var withChat = false

        options.forEach {
            when(it) {
                is Option.MICROPHONE -> micToggled = it.toggled
                is Option.CAMERA -> cameraToggled = it.toggled
                is Option.ZOOM -> withZoom = true
                is Option.PARTICIPANTS -> withParticipants = true
                is Option.CHAT -> withChat = true
            }
        }

        return CallAction.getActions(requireContext(), micToggled, cameraToggled, withZoom, withParticipants, withChat)
    }

    override fun onDismiss() = Unit

    override fun onTap() = onTap(itemAdapter!!.getAdapterItem(currentMenuItemIndex).action)

    private fun onTap(action: CallAction) = when (action) {
        is CallAction.MICROPHONE -> true.also {
            viewModel.requestPermissions(requireActivity())
            viewModel.enableMic(!viewModel.micEnabled.value)
        }
        is CallAction.CAMERA -> true.also {
            viewModel.requestPermissions(requireActivity())
            viewModel.enableCamera(!viewModel.cameraEnabled.value)
        }
        is CallAction.VOLUME -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToVolumeFragment(args.enableTilt)) }
        is CallAction.ZOOM -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToZoomFragment(args.enableTilt)) }
        is CallAction.PARTICIPANTS -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToParticipantsFragment(args.enableTilt)) }
        is CallAction.CHAT -> true.also { findNavController().safeNavigate(MenuFragmentDirections.actionMenuFragmentToSmartglassNavGraphChat(args.enableTilt)) }
        else -> false
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if (it) binding.bandyerMenu.horizontalSmoothScrollToNext(currentMenuItemIndex) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if (it) binding.bandyerMenu.horizontalSmoothScrollToPrevious(currentMenuItemIndex) }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerMenu.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)
}