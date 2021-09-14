/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import com.bandyer.sdk_design.call.bottom_sheet.items.CallAction
import com.bandyer.sdk_design.extensions.getCallThemeAttribute
import com.bandyer.sdk_design.extensions.getSmartGlassMenuDialogAttribute
import com.bandyer.sdk_design.smartglass.call.menu.utils.MotionEventInterceptor
import com.bandyer.sdk_design.smartglass.call.menu.utils.motionEventInterceptor

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
            val smartGlassActionItemMenu = SmartGlassActionItemMenu()
            smartGlassActionItemMenu.items = items
            smartGlassActionItemMenu.show(appCompatActivity.supportFragmentManager, "$TAG ${System.currentTimeMillis()}")
            return smartGlassActionItemMenu
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

    /**
     * Motion events interceptor for motion events that are generated from the smart glass action menu
     */
    var motionEventInterceptor: MotionEventInterceptor? = null
        set(value) {
            field = value
            smartglassMenuLayout?.motionEventInterceptor = field
        }

    /**
     * On create
     *
     * @param savedInstanceState
     * @suppress
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
            return
        }
        setStyle(STYLE_NO_TITLE, requireContext().getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_smartGlassDialogMenuStyle))
    }

    /**
     * On create view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * @suppress
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        smartglassMenuLayout = SmartGlassMenuLayout(ContextThemeWrapper(requireContext(), requireContext().getSmartGlassMenuDialogAttribute(R.styleable.BandyerSDKDesign_SmartGlassDialogMenu_bandyer_smartGlassMenuStyle)))
        smartglassMenuLayout!!.items = items ?: listOf()
        motionEventInterceptor?.let { smartglassMenuLayout!!.motionEventInterceptor = motionEventInterceptor }
        return smartglassMenuLayout!!
    }

    /**
     * On dismiss
     *
     * @param dialog
     * @suppress
     */
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        selectionListener?.onDismiss()
        smartglassMenuLayout?.motionEventInterceptor = null
    }

    /**
     * On destroy
     * @suppress
     */
    override fun onDestroy() {
        super.onDestroy()
        items?.forEach { it.itemView = null }
        items = null
        smartglassMenuLayout?.motionEventInterceptor = null
        smartglassMenuLayout = null
        selectionListener = null
    }
}

