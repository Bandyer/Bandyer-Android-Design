package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentEndCallBinding
import com.bandyer.video_android_glass_ui.BandyerGlassBaseFragment
import com.bandyer.video_android_glass_ui.bottom_action_bar.BandyerBottomActionBarView
import com.google.android.material.textview.MaterialTextView

/**
 * BandyerGlassEndCallFragment. A base class for the end call fragment.
 */
abstract class BandyerGlassEndCallFragment: BandyerGlassBaseFragment() {

    private var binding: BandyerFragmentEndCallBinding? = null

    protected var root: View? = null
    protected var title: MaterialTextView? = null
    protected var subtitle: MaterialTextView? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentEndCallBinding.inflate(inflater, container, false)
        root = binding!!.root
        title = binding!!.bandyerTitle
        subtitle = binding!!.bandyerSubtitle
        bottomActionBar = binding!!.bandyerBottomActionBar
        return root!!
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        root = null
        title = null
        subtitle = null
        bottomActionBar = null
    }
}