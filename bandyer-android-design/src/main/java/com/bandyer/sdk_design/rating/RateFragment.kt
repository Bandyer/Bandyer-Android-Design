package com.bandyer.sdk_design.rating

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bandyer.sdk_design.databinding.FragmentRateBinding

class RateFragment : Fragment() {

    private var _binding: FragmentRateBinding? = null
    private val binding: FragmentRateBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateBinding.inflate(inflater, container, false)
        return binding.root
    }
}