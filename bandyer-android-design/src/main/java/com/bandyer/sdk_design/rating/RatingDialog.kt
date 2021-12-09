package com.bandyer.sdk_design.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerRatingDialogLayoutBinding

class RatingDialog : DialogFragment() {

    private var _binding: BandyerRatingDialogLayoutBinding? = null
    private val binding: BandyerRatingDialogLayoutBinding
        get() = _binding!!

    private var onRatingConfirmedCallback: ((Float, String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BandyerRatingDialogLayoutBinding.inflate(inflater, container, false).apply {
            with(bandyerRatingLayout) {
                bandyerRating.onRatingChangeListener = object : RatingBar.OnRatingChangeListener {
                    override fun onRatingChange(rating: Float) {
                        bandyerSubtitle.text =
                            when {
                                rating <= 1f -> "Molto male"
                                rating > 1f && rating <= 2f -> "Scadente"
                                rating > 2f && rating <= 3f -> "Neutro"
                                rating > 3f && rating <= 4f -> "Buono"
                                else -> "Eccellente"
                            }
                    }
                }
                bandyerClose.setOnClickListener { dismiss() }
                bandyerVote.setOnClickListener {
                    root.visibility = View.GONE
                    bandyerRatingDoneLayout.root.visibility = View.VISIBLE
                    onRatingConfirmedCallback?.invoke(bandyerRating.getRating(), bandyerEdittext.text?.toString() ?: "")
                }
                bandyerRating.setRating(5f)
            }
            bandyerRatingDoneLayout.bandyerClose.setOnClickListener { dismiss() }
        }
        return binding.root
    }

    fun onRatingConfirmed(function: (Float, String) -> Unit) { onRatingConfirmedCallback = function }

    companion object {
        const val TAG = "RatingDialog"
    }
}