package com.bandyer.sdk_design.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bandyer.sdk_design.databinding.FragmentRateAfterBinding

class RateAfterFragment : Fragment() {
    private var _binding: FragmentRateAfterBinding? = null
    private val binding: FragmentRateAfterBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRateAfterBinding.inflate(inflater, container, false)
        return binding.root
    }
}