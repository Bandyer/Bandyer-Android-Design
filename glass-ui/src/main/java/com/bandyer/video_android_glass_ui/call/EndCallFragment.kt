package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.ProvidersHolder
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import kotlinx.coroutines.flow.collect

/**
 * EndCallFragment
 */
class EndCallFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenDialogBinding get() = _binding!!

    @Suppress("UNCHECKED_CAST")
    private val viewModel: NavGraphViewModel by navGraphViewModels(R.id.smartglass_nav_graph) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                NavGraphViewModel(ProvidersHolder.callProvider!!) as T
        }
    }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Apply theme wrapper and add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_endCallStyle)
        _binding = BandyerGlassFragmentFullScreenDialogBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            )
            .apply {
                if(GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()

                lifecycleScope.launchWhenCreated {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.callState.collect { state ->
                            when (state) {
                                is Call.State.Disconnected.Ended -> requireActivity().finish()
                                is Call.State.Disconnected.Error -> requireActivity().finish()
                                else -> Unit
                            }
                        }
                    }
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

    override fun onTap() = true.also { viewModel.hangUp() }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}