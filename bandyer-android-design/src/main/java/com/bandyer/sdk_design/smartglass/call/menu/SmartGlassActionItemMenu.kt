/*
 *  Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 *  See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.smartglass.call.menu

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.extensions.getSmartGlassMenuDialogAttribute
import com.bandyer.sdk_design.extensions.getTextEditorDialogAttribute
import java.util.*

/**
 * A smart glass swipeable menu widget. Selection happens with a tap on the desired item, dismiss happens with a swipe down gesture
 * @property selectionListener OnSmartglassMenuSelectionListener?
 */
class SmartGlassActionItemMenu : DialogFragment() {

    companion object {
        private const val TAG = "SmartglassActionItemMenu"
        private const val ITEMS = "items"

        /**
         * Shows the smartglass swipeable menu
         * @param appCompatActivity AppCompatActivity context
         * @param items List<CallAction> the action items to be displayed
         * @return SmartglassActionItemMenu
         */
        fun show(appCompatActivity: AppCompatActivity, items: List<CallAction>): SmartGlassActionItemMenu {
            val dialogFragment = SmartGlassActionItemMenu()
            dialogFragment.arguments = Bundle().apply {
                this.putSerializable(ITEMS, ArrayList<ActionItem>().apply { this.addAll(items) })
            }
            dialogFragment.show(appCompatActivity.supportFragmentManager, "$TAG ${System.currentTimeMillis()}")
            return dialogFragment
        }
    }

    private var items: List<CallAction>? = null

    private var smartglassMenuLayout: SmartGlassMenuLayout? = null
        set(value) {
            field = value
            field ?: return
            selectionListener?.let {
                field!!.onSmartglassMenuSelectionListener = it
            }
        }

    /**
     * Smart glass menu selection listener
     */
    var selectionListener: SmartGlassMenuLayout.OnSmartglassMenuSelectionListener? = null
        set(value) {
            field = value
            smartglassMenuLayout?.onSmartglassMenuSelectionListener = selectionListener
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_smartGlassDialogMenuStyle))
        val itemsArrayList = arguments?.getSerializable(ITEMS)
        items = mutableListOf<CallAction>().apply {
            this.addAll(itemsArrayList as ArrayList<CallAction>)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        smartglassMenuLayout = SmartGlassMenuLayout(ContextThemeWrapper(requireContext(), requireContext().getSmartGlassMenuDialogAttribute(R.styleable.BandyerSDKDesign_SmartGlassDialogMenu_bandyer_smartGlassMenuStyle)))
        smartglassMenuLayout!!.items = items
        return smartglassMenuLayout!!
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        selectionListener?.onDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        items?.forEach { it.itemView = null }
        items = null
        smartglassMenuLayout = null
        selectionListener = null
    }
}

