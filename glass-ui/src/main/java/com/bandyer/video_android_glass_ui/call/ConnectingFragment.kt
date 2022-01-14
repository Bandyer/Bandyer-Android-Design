package com.bandyer.video_android_glass_ui.call

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
import com.bandyer.video_android_glass_ui.common.ReadProgressDecoration
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.model.CallParticipant
import com.bandyer.video_android_glass_ui.model.Stream
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.*

internal abstract class ConnectingFragment : BaseFragment() {

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
                        call.participants.onEach { participants ->
                                val isGroupCall = nOfParticipants > 2
                                // TODO userDetails
                                val items = ((if (isGroupCall) listOf(participants.me) else listOf()).plus(participants.others)).map { FullScreenDialogItem(it.userAlias) }
                                FastAdapterDiffUtil[itemAdapter!!] = FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)

                                if (nOfParticipants == itemAdapter!!.adapterItemCount) return@onEach
                                nOfParticipants = itemAdapter!!.adapterItemCount
                                if (nOfParticipants < 2) bandyerBottomNavigation.hideSwipeHorizontalItem()
                                else bandyerCounter.text = resources.getString(
                                    R.string.bandyer_glass_n_of_participants_pattern,
                                    participants.others.size + 1
                                )

                                setSubtitle(nOfParticipants > 2)
                            }
                            .launchIn(this@repeatOnStarted)

                        liveStreams
                            .dropWhile { it.count() > 0 }
                            .takeWhile { it.count() < 1 }
                            .onCompletion {  onLiveStream() }
                            .launchIn(this@repeatOnStarted)

                        inCallParticipants
                            .takeWhile { it.count() < 2 }
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

    abstract fun onLiveStream()

    abstract fun setSubtitle(isGroupCall: Boolean)
}