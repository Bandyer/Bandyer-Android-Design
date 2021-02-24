package com.bandyer.sdk_design.screensharing.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.BandyerBottomSheetDialog
import com.bandyer.sdk_design.bottom_sheet.behaviours.BandyerBottomSheetBehaviour
import com.bandyer.sdk_design.dialogs.BandyerDialog
import com.bandyer.sdk_design.extensions.getCallThemeAttribute

/**
 * Bandyer screen share picker dialog
 *
 * @constructor Create empty Bandyer screen share picker dialog
 */
class BandyerScreenSharePickerDialog : BandyerDialog<BandyerBottomSheetDialog> {

    override var dialog: BandyerBottomSheetDialog? = null

    override val id: String = "BandyerScreenSharePickerDialog"

    /**
     * Sharing option
     *
     * @constructor Create empty Sharing option
     */
    enum class SharingOption {

        /**
         * A p p_o n l y
         *
         * @constructor Create empty A p p_o n l y
         */
        APP_ONLY,

        /**
         * W h o l e_d e v i c e
         *
         * @constructor Create empty W h o l e_d e v i c e
         */
        WHOLE_DEVICE
    }

    override fun show(activity: androidx.fragment.app.FragmentActivity) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = PickerDialog()
        dialog!!.show(activity.supportFragmentManager, id)
        activity.supportFragmentManager.executePendingTransactions()
    }

    /**
     * Show the dialog
     *
     * @param activity launching
     * @param onChoose callback for the option chosen
     * @receiver
     */
    fun show(activity: androidx.fragment.app.FragmentActivity, onChoose: (sharingOption: SharingOption) -> Unit) {
        if (dialog?.isVisible == true || dialog?.isAdded == true) return
        if (dialog == null) dialog = PickerDialog().apply {
            this.onChoose = onChoose
        }
        show(activity)
    }

    internal class PickerDialog : BandyerBottomSheetDialog() {

        override val isDraggable = true
        override val isDismissable = true

        var onChoose: ((sharingOption: SharingOption) -> Unit)? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(DialogFragment.STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_screenShareDialogStyle))
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.bandyer_screen_share_picker_dialog_layout, container).apply {
                findViewById<View>(R.id.bandyer_app_screen_share)?.setOnClickListener {
                    onChoose?.invoke(SharingOption.APP_ONLY)
                    dismiss()
                }
                findViewById<View>(R.id.bandyer_device_screen_share)?.setOnClickListener {
                    onChoose?.invoke(SharingOption.WHOLE_DEVICE)
                    dismiss()
                }
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            onChoose = null
        }

        override fun onExpanded() = Unit

        override fun onCollapsed() = dismiss()

        override fun onDialogWillShow() = Unit

        override fun onSlide(offset: Float) = Unit

        override fun onStateChanged(newState: Int) {
            if (newState == BandyerBottomSheetBehaviour.STATE_HIDDEN) dismiss()
        }
    }
}