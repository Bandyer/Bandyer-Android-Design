package com.bandyer.sdk_design.feedback

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
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
    private var onDismissCallback: (() -> Unit)? = null

    private var autoDismissTime: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().obtainStyledAttributes(R.style.BandyerSDKDesign_FragmentDialog, R.styleable.BandyerSDKDesign_FragmentDialog).apply {
            autoDismissTime = getInt(R.styleable.BandyerSDKDesign_FragmentDialog_bandyer_autoDismissTime, -1)
            recycle()
        }
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
                        if(view != bandyerInputLayout) return
                        bandyerEdittext.requestFocus()
                    }
                })
                bandyerEdittext.apply {
                    setOnFocusChangeListener { _, hasFocus ->
                        if(!hasFocus) return@setOnFocusChangeListener
                        bandyerTitle.visibility = View.GONE
                        this.setLines(4)
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
                    onFeedbackCallback?.invoke(bandyerRating.getRating(), bandyerEdittext.text?.toString()?.trim() ?: "")
                    with(bandyerFragmentFeedbackSentLayout.root) {
                        visibility = View.VISIBLE
                        if(autoDismissTime == -1) return@setOnClickListener
                        postDelayed(autoDismissTime.toLong()) { dismiss() }
                    }
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
     * @param function Function0<Float, Unit>
     * @return FeedbackDialog
     */
    fun onRate(function: (Float) -> Unit): FeedbackDialog { onRateCallback = function; return this }

    /**
     * Set the callback invoked on comment change
     *
     * @param function Function0<Float, Unit>
     * @return FeedbackDialog
     */
    fun onComment(function: (String) -> Unit): FeedbackDialog { onCommentCallback = function; return this }

    /**
     * Set the callback invoked on feedback confirmed
     *
     * @param function Function0<Float, Unit>
     * @return FeedbackDialog
     */
    fun onFeedback(function: (Float, String) -> Unit): FeedbackDialog { onFeedbackCallback = function; return this }

    /**
     * Set the callback invoked on fragment dialog dismiss
     *
     * @param function Function0<Unit>
     * @return FeedbackDialog
     */
    fun onDismiss(function: () -> Unit): FeedbackDialog { onDismissCallback = function; return this }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        onRateCallback = null
        onCommentCallback = null
        onFeedbackCallback = null
        onDismissCallback = null
    }

    companion object {
        const val TAG = "FeedbackDialog"
    }
}