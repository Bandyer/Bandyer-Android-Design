package com.bandyer.sdk_design.feedback

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerFeedbackDialogLayoutBinding
import com.bandyer.sdk_design.extensions.getCallThemeAttribute

/**
 * FeedbackDialog
 */
class FeedbackDialog : DialogFragment() {

    private lateinit var binding: BandyerFeedbackDialogLayoutBinding

    private var onRateCallback: ((Float) -> Unit)? = null
    private var onCommentCallback: ((String) -> Unit)? = null
    private var onFeedbackCallback: ((Float, String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_feedbackDialogStyle))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFeedbackDialogLayoutBinding.inflate(inflater, container, false).apply {
            with(bandyerFragmentFeedbackLayout) {
                bandyerRating.onRatingChangeListener = object : RatingBar.OnRatingChangeListener {
                    override fun onRatingChange(rating: Float) {
                         val ratingText = resources.getString(
                            when {
                                rating <= 1f -> R.string.bandyer_feedback_bad
                                rating <= 2f -> R.string.bandyer_feedback_poor
                                rating <= 3f -> R.string.bandyer_feedback_neutral
                                rating <= 4f -> R.string.bandyer_feedback_good
                                else -> R.string.bandyer_feedback_excellent
                            })
                        with(bandyerSubtitle) {
                            text = ratingText
                            contentDescription = ratingText
                        }
                    }

                    override fun onRatingConfirmed(rating: Float) { onRateCallback?.invoke(rating) }
                }
                root.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
                    override fun startTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) = Unit
                    override fun endTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
                        if(view == bandyerInputLayout)
                            bandyerEdittext.requestFocus()
                    }
                })
                bandyerEdittext.apply {
                    setOnFocusChangeListener { _, hasFocus ->
                        if(hasFocus) {
                            bandyerTitle.visibility = View.GONE
                            this.setLines(4)
                        }
                    }

                    addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
                        override fun afterTextChanged(s: Editable) { onCommentCallback?.invoke(s.toString().trim()) }
                    })
                }
                bandyerClose.setOnClickListener { dismiss() }
                bandyerVote.setOnClickListener {
                    root.visibility = View.GONE
                    bandyerFragmentFeedbackSentLayout.root.visibility = View.VISIBLE
                    onFeedbackCallback?.invoke(bandyerRating.getRating(), bandyerEdittext.text?.toString()?.trim() ?: "")
                }
                bandyerRating.setRating(5f)
                bandyerTitle.requestFocus()
            }
            bandyerFragmentFeedbackSentLayout.bandyerClose.setOnClickListener { dismiss() }
        }
        return binding.root
    }

    /**
     * Set the callback invoked on rating change
     *
     * @param function Function1<Float, Unit>
     * @return FeedbackDialog
     */
    fun onRate(function: (Float) -> Unit): FeedbackDialog { onRateCallback = function; return this }

    /**
     * Set the callback invoked on comment change
     *
     * @param function Function1<Float, Unit>
     * @return FeedbackDialog
     */
    fun onComment(function: (String) -> Unit): FeedbackDialog { onCommentCallback = function; return this }

    /**
     * Set the callback invoked on feedback confirmed
     *
     * @param function Function1<Float, Unit>
     * @return FeedbackDialog
     */
    fun onFeedback(function: (Float, String) -> Unit): FeedbackDialog { onFeedbackCallback = function; return this }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onRateCallback = null
        onCommentCallback = null
        onFeedbackCallback = null
    }

    companion object {
        const val TAG = "FeedbackDialog"
    }
}