package com.bandyer.video_android_glass_ui.call

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.*

internal abstract class ConnectingFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    protected val viewModel: GlassViewModel by activityViewModels {
        GlassViewModelFactory.getInstance(
            GlassUIProvider.callService!!.get() as CallUIDelegate,
            GlassUIProvider.callService!!.get() as DeviceStatusDelegate,
            GlassUIProvider.callService!!.get() as CallUIController
        )
    }

    abstract val themeResId: Int

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onHangup()
        }

        // Add view binding
        _binding = BandyerGlassFragmentFullScreenLogoDialogBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            )
            .apply {
                if (GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()

                repeatOnStarted {
                    viewModel.amIAlone
                        .onEach { if (!it) onConnected() }
                        .takeWhile { it }
                        .launchIn(this@repeatOnStarted)

                    viewModel.inCallParticipants
                        .takeWhile { it.count() < 2 }
                        .onCompletion {
                            bandyerSubtitle.text =
                                resources.getString(R.string.bandyer_glass_connecting)
                        }.launchIn(this@repeatOnStarted)
                }
            }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun onConnected()

    abstract fun setSubtitle(isGroupCall: Boolean)
}