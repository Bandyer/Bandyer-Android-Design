package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.TouchEventListener
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentEmptyBinding

/**
 * EmptyFragment. A base class for the call fragment.
 */
abstract class EmptyFragment: BaseFragment(), TouchEventListener {

    private var _binding: BandyerGlassFragmentEmptyBinding? = null
    private val binding get() = _binding!!

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BandyerGlassFragmentEmptyBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
