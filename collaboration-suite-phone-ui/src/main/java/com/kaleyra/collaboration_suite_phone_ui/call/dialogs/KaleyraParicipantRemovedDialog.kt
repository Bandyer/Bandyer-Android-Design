package com.kaleyra.collaboration_suite_phone_ui.call.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.postDelayed
import androidx.fragment.app.DialogFragment
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraCallParticipantRemovedDialogLayoutBinding
import com.kaleyra.collaboration_suite_phone_ui.extensions.getCallThemeAttribute
import com.kaleyra.collaboration_suite_utils.ContextRetainer

/**
 * Call participant removed dialog
 */
class KaleyraParicipantRemovedDialog(private val adminDisplayName: String? = null) : DialogFragment() {

    private lateinit var binding: KaleyraCallParticipantRemovedDialogLayoutBinding

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
        setStyle(STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_callParticipantRemovedStyle))
    }

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = KaleyraCallParticipantRemovedDialogLayoutBinding.inflate(inflater, container, false)
        binding.kaleyraTitle.text = ContextRetainer.context.resources.getQuantityString(R.plurals.kaleyra_call_participant_removed, if (adminDisplayName != null) 1 else 0, adminDisplayName)
        binding.kaleyraTitle.requestFocus()
        binding.kaleyraTitle.setOnClickListener { dismiss() }
        binding.root.setOnClickListener { dismiss() }
        if(autoDismissTime != -1) binding.root.postDelayed(autoDismissTime.toLong()) { dismiss() }
        return binding.root
    }

    /**
     * Set the callback invoked on fragment dialog dismiss
     *
     * @param function Function0<Unit>
     * @return FeedbackDialog
     */
    fun onDismiss(function: () -> Unit): KaleyraParicipantRemovedDialog { onDismissCallback = function; return this }

    /**
     * @suppress
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
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