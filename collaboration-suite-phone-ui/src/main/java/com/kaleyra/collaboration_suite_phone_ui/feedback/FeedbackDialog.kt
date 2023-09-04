/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.feedback

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.core.graphics.ColorUtils
import androidx.core.view.postDelayed
import androidx.fragment.app.DialogFragment
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraFeedbackDialogLayoutBinding
import java.lang.reflect.Field


/**
 * FeedbackDialog
 */
class FeedbackDialog(val colors: Colors, val fontFamily: FontFamily) : DialogFragment() {

    private lateinit var binding: KaleyraFeedbackDialogLayoutBinding

    private var onRateCallback: ((Float) -> Unit)? = null
    private var onCommentCallback: ((String) -> Unit)? = null
    private var onFeedbackCallback: ((Float, String) -> Unit)? = null
    private var onDismissCallback: (() -> Unit)? = null

    private var autoDismissTime: Int = -1

    /**
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().obtainStyledAttributes(R.style.KaleyraCollaborationSuiteUI_FragmentDialog, R.styleable.KaleyraCollaborationSuiteUI_FragmentDialog).apply {
            autoDismissTime = getInt(R.styleable.KaleyraCollaborationSuiteUI_FragmentDialog_kaleyra_autoDismissTime, -1)
            recycle()
        }
        setStyle(STYLE_NO_TITLE, R.style.KaleyraCollaborationSuiteUI_FragmentDialog_Feedback)
    }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = KaleyraFeedbackDialogLayoutBinding.inflate(inflater, container, false).apply {
            with(kaleyraFragmentFeedbackLayout) {
                val normalTypeFace = createFontFamilyResolver(requireContext()).resolve(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal
                ).value as Typeface
                val boldTypeFace = createFontFamilyResolver(requireContext()).resolve(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold
                ).value as Typeface
                kaleyraTitle.typeface = normalTypeFace
                kaleyraSubtitle.typeface = boldTypeFace
                kaleyraEdittext.typeface = normalTypeFace

                val surface = colors.surface.toArgb()
                val onSurface = colors.onSurface.toArgb()
                val secondary = colors.secondary.toArgb()
                root.setBackgroundColor(surface)
                kaleyraClose.iconTint = ColorStateList.valueOf(onSurface)
                kaleyraVote.setBackgroundColor(secondary)
                kaleyraTitle.setTextColor(ColorUtils.setAlphaComponent(onSurface, (.8f * 255).toInt()))
                kaleyraSubtitle.setTextColor(onSurface)
                kaleyraRating.setDrawableBackgroundTint(secondary)
                kaleyraRating.setDrawableProgressTint(secondary)
                val editTextBackgroundDrawable = kaleyraEdittext.background as GradientDrawable
                editTextBackgroundDrawable.mutate()
                editTextBackgroundDrawable.color = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_focused),
                        intArrayOf()
                    ),
                    intArrayOf(
                        ColorUtils.setAlphaComponent(onSurface, (.12f * 255).toInt()),
                        Color.Transparent.toArgb()
                    )
                )
                val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, resources.displayMetrics)
                editTextBackgroundDrawable.setStroke(
                    strokeWidth.toInt(),
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_focused),
                            intArrayOf()
                        ),
                        intArrayOf(
                            Color.Transparent.toArgb(),
                            ColorUtils.setAlphaComponent(onSurface, (.3f * 255).toInt())
                        )
                    )
                )
                kaleyraEdittext.setHintTextColor(ColorUtils.setAlphaComponent(onSurface, (.5f * 255).toInt()))
                kaleyraEdittext.setTextColor(onSurface)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    kaleyraEdittext.setTextCursorDrawable(secondary)
//                } else {
//                    try {
//                        val f: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
//                        f.isAccessible = true
////                        f.set(kaleyraEdittext, R.drawable.cursor)
//                    } catch (ignored: Exception) {
//                    }
//                }



                kaleyraRating.onRatingChangeListener = object : RatingBar.OnRatingChangeListener {
                    override fun onRatingChange(rating: Float) {
                        val ratingText = resources.getString(
                            when {
                                rating <= 1f -> R.string.kaleyra_feedback_bad
                                rating <= 2f -> R.string.kaleyra_feedback_poor
                                rating <= 3f -> R.string.kaleyra_feedback_neutral
                                rating <= 4f -> R.string.kaleyra_feedback_good
                                else -> R.string.kaleyra_feedback_excellent
                            })

                        with(kaleyraSubtitle) {
                            text = ratingText
                            contentDescription = ratingText
                        }
                    }

                    override fun onRatingConfirmed(rating: Float) { onRateCallback?.invoke(rating) }
                }
                root.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
                    override fun startTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) = Unit
                    override fun endTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
                        if(view != kaleyraInputLayout) return
                        kaleyraEdittext.requestFocus()
                    }
                })
                kaleyraEdittext.apply {
                    setOnFocusChangeListener { _, hasFocus ->
                        if(!hasFocus) return@setOnFocusChangeListener
                        kaleyraTitle.visibility = View.GONE
                        this.setLines(4)
                    }

                    addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
                        override fun afterTextChanged(s: Editable) { onCommentCallback?.invoke(s.toString().trim()) }
                    })
                }
                kaleyraClose.setOnClickListener { dismiss() }
                kaleyraVote.setOnClickListener {
                    root.visibility = View.GONE
                    onFeedbackCallback?.invoke(kaleyraRating.getRating(), kaleyraEdittext.text?.toString()?.trim() ?: "")
                    with(kaleyraFragmentFeedbackSentLayout.root) {
                        visibility = View.VISIBLE
                        if(autoDismissTime == -1) return@setOnClickListener
                        postDelayed(autoDismissTime.toLong()) { dismiss() }
                    }
                }
                kaleyraRating.setRating(5f)
                kaleyraTitle.requestFocus()
            }
            kaleyraFragmentFeedbackSentLayout.kaleyraClose.setOnClickListener { dismiss() }
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

    /**
     * @suppress
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
        onRateCallback = null
        onCommentCallback = null
        onFeedbackCallback = null
        onDismissCallback = null
    }

    /**
     * Feedback dialog companion object
     */
    companion object {
        /**
         * Feedback dialog logging tag
         */
        const val TAG = "FeedbackDialog"
    }
}