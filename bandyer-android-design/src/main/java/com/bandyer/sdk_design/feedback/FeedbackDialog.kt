package com.bandyer.sdk_design.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFeedbackDialogLayoutBinding
import com.bandyer.sdk_design.extensions.getCallThemeAttribute

class FeedbackDialog : DialogFragment() {

    private var _binding: BandyerFeedbackDialogLayoutBinding? = null
    private val binding: BandyerFeedbackDialogLayoutBinding
        get() = _binding!!

    private var onRatingConfirmedCallback: ((Float, String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_feedbackDialogStyle))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BandyerFeedbackDialogLayoutBinding.inflate(inflater, container, false).apply {
            with(fragmentFeedbackLayout) {
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
                bandyerSend.setOnClickListener {
                    root.visibility = View.GONE
                    fragmentFeedbackSentLayout.root.visibility = View.VISIBLE
                    onRatingConfirmedCallback?.invoke(bandyerRating.getRating(), bandyerEdittext.text?.toString() ?: "")
                }
                bandyerRating.setRating(5f)
            }
            fragmentFeedbackSentLayout.bandyerClose.setOnClickListener { dismiss() }
        }
        return binding.root
    }

    fun onRatingConfirmed(function: (Float, String) -> Unit) { onRatingConfirmedCallback = function }

    companion object {
        const val TAG = "FeedbackDialog"
    }
}