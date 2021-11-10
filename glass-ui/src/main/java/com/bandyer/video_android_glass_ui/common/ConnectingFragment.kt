package com.bandyer.video_android_glass_ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.call.AutoScrollLinearLayoutManager
import com.bandyer.video_android_glass_ui.call.FullScreenDialogItem
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.*

internal abstract class ConnectingFragment: BaseFragment() {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<FullScreenDialogItem>? = null

    protected val viewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

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
            viewModel.hangUp()
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

                // Init the RecyclerView
                bandyerParticipants.apply {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager = AutoScrollLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    addItemDecoration(ReadProgressDecoration(requireContext()))

                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }

                repeatOnStarted {
                    with(viewModel) {
                        var nOfParticipants = 0
                        callState
                            .combine(participants) { state, participants ->
                                if(state is Call.State.Connected) onConnected()
                                
                                val items = participants.others.plus(participants.me).map { FullScreenDialogItem(it.username) }
                                FastAdapterDiffUtil[itemAdapter!!] = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)

                                if(nOfParticipants == itemAdapter!!.adapterItemCount) return@combine
                                nOfParticipants = itemAdapter!!.adapterItemCount
                                if (nOfParticipants < 2) bandyerBottomNavigation.hideSwipeHorizontalItem()
                                else bandyerCounter.text = resources.getString(R.string.bandyer_glass_n_of_participants_pattern, participants.others.size + 1)

                                setSubtitle(nOfParticipants > 1)
                            }
                            .launchIn(this@repeatOnStarted)

                        participants
                            .map { it.others + it.me }
                            .flatMapConcat { participants -> participants.map { it.state }.merge() }
                            .takeWhile { it !is CallParticipant.State.Online.InCall }
                            .onCompletion { bandyerSubtitle.text = resources.getString(R.string.bandyer_glass_connecting) }
                            .launchIn(this@repeatOnStarted)
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
        itemAdapter = null
    }

    abstract fun onConnected()

    abstract fun setSubtitle(isGroupCall: Boolean)
}