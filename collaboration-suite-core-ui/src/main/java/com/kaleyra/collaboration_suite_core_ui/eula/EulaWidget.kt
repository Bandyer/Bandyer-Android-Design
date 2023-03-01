package com.kaleyra.collaboration_suite_core_ui.eula

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.databinding.KaleyraEulaBinding

class EulaWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: KaleyraEulaBinding by lazy { KaleyraEulaBinding.inflate(LayoutInflater.from(context), this, true) }

    fun setTitle(text: String) {
        binding.kaleyraTitle.text = text
    }

    fun setMessage(text: String) {
        binding.kaleyraMessage.text = text
    }

    fun setAcceptButtonText(text: String) {
        binding.kaleyraAcceptButton.text = text
    }

    fun setDeclineButtonText(text: String) {
        binding.kaleyraDeclineButton.text = text
    }

    fun setAcceptButtonListener(block: () -> Unit) {
        binding.kaleyraAcceptButton.setOnClickListener {
            block.invoke()
        }
    }

    fun setDeclineButtonListener(block: () -> Unit) {
        binding.kaleyraAcceptButton.setOnClickListener {
            block.invoke()
        }
    }
}