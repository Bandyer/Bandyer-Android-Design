package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.common.ReadProgressDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogNewBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class RingingFragment : BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogNewBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogNewBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<FullScreenDialogItem>? = null

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        // Add view binding
        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_ringingStyle)
        _binding = BandyerGlassFragmentFullScreenLogoDialogNewBinding
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
                    val layoutManager = AutoScrollLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

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

    override fun onTap() =
        true.also { findNavController().navigate(R.id.action_ringingFragment_to_emptyFragment) }

    override fun onSwipeDown() = true.also { requireActivity().finish() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { binding.bandyerParticipants.smoothScrollBy(resources.displayMetrics.densityDpi / 2, 0) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { binding.bandyerParticipants.smoothScrollBy(-resources.displayMetrics.densityDpi / 2, 0) }
}