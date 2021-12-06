package com.bandyer.sdk_design.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.bandyer.sdk_design.databinding.FragmentRateAfterBinding

class RateAfterFragment : Fragment() {
    private var _binding: FragmentRateAfterBinding? = null
    private val binding: FragmentRateAfterBinding
        get() = _binding!!

    private val parentDialogFragment: DialogFragment
        get() = requireParentFragment().requireParentFragment() as DialogFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRateAfterBinding.inflate(inflater, container, false).apply {
            bandyerButton.setOnClickListener { parentDialogFragment.dismiss() }
        }
        return binding.root
    }
}