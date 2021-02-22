/*
 *  Copyright (C) 2020 Bandyer S.r.l. All Rights Reserved.
 *  See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.call.smartglass

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.bottom_sheet.items.ActionItem
import com.bandyer.sdk_design.call.bottom_sheet.items.SmartglassCallAction
import java.util.*

/**
 * A smartglass swipeable menu widget. Selection happens with a tap on the desired item, dismiss happens with a swipe down gesture
 * @property items List<ActionItem>?
 * @property smartglassMenuLayout SmartglassesMenuLayout?
 * @property selectionListener OnSmartglassMenuSelectionListener?
 */
class SmartglassActionItemMenu : DialogFragment() {

    companion object {
        private const val TAG = "SmartglassActionItemMenu"
        private const val ITEMS = "items"

        /**
         * Shows the smartglass swipeable menu
         * @param appCompatActivity AppCompatActivity context
         * @param items List<ActionItem> the action items to be displayed
         * @return SmartglassActionItemMenu
         */
        fun show(appCompatActivity: AppCompatActivity, items: List<SmartglassCallAction>): SmartglassActionItemMenu {
            val dialogFragment = SmartglassActionItemMenu()
            dialogFragment.arguments = Bundle().apply {
                this.putSerializable(ITEMS, ArrayList<ActionItem>().apply { this.addAll(items) })
            }
            dialogFragment.show(appCompatActivity.supportFragmentManager, "$TAG ${System.currentTimeMillis()}")
            return dialogFragment
        }
    }

    private var items: List<SmartglassCallAction>? = null

    private var smartglassMenuLayout: SmartglassesMenuLayout? = null
        set(value) {
            field = value
            field ?: return
            selectionListener?.let {
                field!!.onSmartglassMenuSelectionListener = it
            }
        }

    /**
     * Smartglass menu selection listener
     */
    var selectionListener: SmartglassesMenuLayout.OnSmartglassMenuSelectionListener? = null
        set(value) {
            field = value
            smartglassMenuLayout?.onSmartglassMenuSelectionListener = selectionListener
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val itemsArrayList = arguments?.getSerializable(ITEMS)
        items = mutableListOf<SmartglassCallAction>().apply {
            this.addAll(itemsArrayList as ArrayList<SmartglassCallAction>)
        }
    }

    override fun getTheme(): Int = R.style.BandyerSDKDesign_DialogFragment_FullScreen

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        smartglassMenuLayout = SmartglassesMenuLayout(
                context!!,
                items!!)
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

