package com.bandyer.sdk_design.new_smartglass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFragmentChatBinding
import com.bandyer.sdk_design.databinding.BandyerFragmentMenuBinding

class MenuFragment : Fragment(), SmartGlassTouchEventListener, BottomBarHolder {

    private lateinit var binding: BandyerFragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean = false

    override fun showBottomBar() {
        binding.bottomActionBar.visibility = View.VISIBLE
    }

    override fun hideBottomBar() {
        binding.bottomActionBar.visibility = View.GONE
    }
}