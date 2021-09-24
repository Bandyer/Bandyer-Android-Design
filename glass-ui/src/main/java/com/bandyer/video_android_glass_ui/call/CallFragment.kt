package com.bandyer.video_android_glass_ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentCallBinding
import com.bandyer.video_android_glass_ui.TouchEventListener
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.bottom_action_bar.BottomActionBarView

/**
 * CallFragment. A base class for the call fragment.
 */
abstract class CallFragment: BaseFragment(),
                             TouchEventListener {

    private var binding: BandyerFragmentCallBinding? = null

    protected var root: View? = null
    protected var bottomActionBar: BottomActionBarView? = null

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentCallBinding.inflate(inflater, container, false)
        root = binding!!.root
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
        bottomActionBar = null
    }
}
