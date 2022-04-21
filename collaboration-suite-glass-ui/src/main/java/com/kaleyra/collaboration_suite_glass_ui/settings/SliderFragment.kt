package com.kaleyra.collaboration_suite_glass_ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentSliderBinding

internal abstract class SliderFragment: BaseFragment() {

    private var _binding: KaleyraGlassFragmentSliderBinding? = null
    override val binding: KaleyraGlassFragmentSliderBinding get() = _binding!!

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

        _binding = KaleyraGlassFragmentSliderBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            ).apply {
                if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation)
                root.setOnTouchListener { _, _ -> true }
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

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setTapOnClickListener { onSwipeForward(true) }
        bottomNavView.setSwipeDownOnClickListener { onTap() }
        bottomNavView.setSwipeHorizontalOnClickListener { onSwipeBackward(true) }
    }
}