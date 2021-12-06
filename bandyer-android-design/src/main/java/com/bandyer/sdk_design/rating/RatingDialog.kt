package com.bandyer.sdk_design.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerRatingDialogLayoutBinding

class RatingDialog : DialogFragment() {

    private var _binding: BandyerRatingDialogLayoutBinding? = null
    private val binding: BandyerRatingDialogLayoutBinding
        get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BandyerRatingDialogLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "RatingDialog"
    }
}