package com.bandyer.video_android_glass_ui.call

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.GlassViewModel
import com.bandyer.video_android_glass_ui.GlassViewModelFactory
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentFullScreenLogoDialogBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.*
import kotlin.math.abs
import kotlin.math.roundToInt

internal abstract class ConnectingFragment : BaseFragment(),
    ViewTreeObserver.OnScrollChangedListener {

    private var _binding: BandyerGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: BandyerGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

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

                with(bandyerParticipantsScrollView) {
                    viewTreeObserver.addOnScrollChangedListener(this@ConnectingFragment)
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }

                repeatOnStarted {
                    with(viewModel) {
                        userDetailsWrapper
                            .onEach {
                                bandyerParticipants.text = it.formatters.callFormatter.format(*it.data.toTypedArray())
                                updateUIOnParticipantChange()
                            }.launchIn(this@repeatOnStarted)

                        call.participants.onEach { participants ->
                            bandyerCounter.text = resources.getString(
                                R.string.bandyer_glass_n_of_participants_pattern,
                                participants.others.size + 1
                            )

                            setSubtitle(participants.others.count() + 1 > 2)
                        }.launchIn(this@repeatOnStarted)

                        amIAlone
                            .onEach { if(!it) onConnected() }
                            .takeWhile { it }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUIOnParticipantChange()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIOnParticipantChange()
    }

    private fun updateUIOnParticipantChange() = with(binding) {
        bandyerParticipants.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        if (resources.displayMetrics.widthPixels - bandyerParticipants.measuredWidth > 0) {
            bandyerBottomNavigation.hideSwipeHorizontalItem()
            bandyerProgress.visibility = View.GONE
        } else {
            bandyerBottomNavigation.showSwipeHorizontalItem()
            bandyerProgress.visibility = View.VISIBLE
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding.bandyerParticipantsScrollView.viewTreeObserver.removeOnScrollChangedListener(this)
        _binding = null
    }

    override fun onScrollChanged() {
        _binding ?: return
        with(binding) {
            bandyerProgress.apply {
                max = bandyerParticipantsScrollView.getChildAt(0).width
                progress = ((bandyerParticipantsScrollView.scrollX + bandyerParticipantsScrollView.width).toFloat()).roundToInt()
            }
        }
    }

    abstract fun onConnected()

    abstract fun setSubtitle(isGroupCall: Boolean)
}