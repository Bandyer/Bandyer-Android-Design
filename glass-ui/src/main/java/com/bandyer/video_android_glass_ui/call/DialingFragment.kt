package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.ProvidersHolder
import com.bandyer.video_android_glass_ui.common.ReadProgressDecoration
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.flow.collect

class DialingFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<FullScreenDialogItem>? = null

    @Suppress("UNCHECKED_CAST")
    private val viewModel: GlassViewModel by navGraphViewModels(R.id.smartglass_nav_graph) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                GlassViewModel(ProvidersHolder.callProvider!!) as T
        }
    }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        // Add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_dialingStyle)
        _binding = BandyerGlassFragmentFullScreenLogoDialogBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            )
            .apply {
                if (GlassDeviceUtils.isRealWear) bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                bandyerParticipants.apply {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager = AutoScrollLinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    setHasFixedSize(true)
                    addItemDecoration(ReadProgressDecoration(requireContext()))

                    /**
                     * TODO
                     * for... itemAdapter.add..
                     */

                    itemAdapter!!.add(
                        FullScreenDialogItem("Mario Rossi"),
                        FullScreenDialogItem("Gianfranco Mazzoni"),
                        FullScreenDialogItem("Andrea Tocchetti"),
                        FullScreenDialogItem("Stefano Brusadelli")
                    )
                }

                val isGroupCall = itemAdapter!!.adapterItemCount > 1
                if (!isGroupCall)
                    bandyerBottomNavigation.hideSwipeHorizontalItem()
                else
                    bandyerCounter.text = resources.getString(
                        R.string.bandyer_glass_n_of_participants_pattern,
                        itemAdapter!!.adapterItemCount
                    )

                bandyerSubtitle.text =
                    resources.getString(if (isGroupCall) R.string.bandyer_glass_dialing_group else R.string.bandyer_glass_dialing)

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

    override fun onTap() = false

    override fun onSwipeDown() = true.also { viewModel.hangUp() }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}