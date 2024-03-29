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

package com.kaleyra.collaboration_suite_phone_ui.widgets

import android.os.CountDownTimer
import com.kaleyra.collaboration_suite_core_ui.widget.HideableWidget

/**
 * A composite of Hideable Widgets
 */
class WidgetHidingCoordinator : HideableWidget {

    /**
     * @suppress
     */
    override var hidingTimer: CountDownTimer? = null

    private var hideableWidgets = mutableListOf<HideableWidget>()

    /**
     * @suppress
     */
    override var millisUntilTimerFinish: Long = 0

    /**
     * Add the widgets you want to behave at the same time
     * @property hideAfterMillis millis to wait before hiding the widgets added
     * @param widgets the widgets that should be bound together
     */
    fun addWidgets(hideAfterMillis: Long, vararg widgets: HideableWidget) {
        hideableWidgets.forEach { it.disableAutoHide() }
        hideableWidgets.clear()
        autoHide(hideAfterMillis)
        widgets.forEach {
            it.hidingTimer = hidingTimer
        }
        hideableWidgets.addAll(widgets)
    }

    /**
     * Remove the specified widget from hiding logic.
     * @param widget the widget to be removed.
     */
    fun removeWidget(widget: HideableWidget) {
        widget.disableAutoHide()
        hideableWidgets.remove(widget)
    }

    /**
     * @suppress
     */
    override fun disableAutoHide() {
        hideableWidgets.forEach { it.disableAutoHide() }
    }

    /**
     * @suppress
     */
    override fun onHidingTimerFinished() {
        hideableWidgets.forEach { it.onHidingTimerFinished() }
    }
}