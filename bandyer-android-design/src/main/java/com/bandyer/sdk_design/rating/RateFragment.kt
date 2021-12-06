package com.bandyer.sdk_design.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.FragmentRateBinding

class RateFragment : Fragment() {

    private var _binding: FragmentRateBinding? = null
    private val binding: FragmentRateBinding
        get() = _binding!!

    private val parentDialogFragment: DialogFragment
        get() = requireParentFragment().requireParentFragment() as DialogFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateBinding.inflate(inflater, container, false).apply {
            bandyerClose.setOnClickListener { parentDialogFragment.dismiss() }
            bandyerButton.setOnClickListener { findNavController().navigate(R.id.action_rateFragment_to_rateAfterFragment)  }
        }

        return binding.root
    }
}