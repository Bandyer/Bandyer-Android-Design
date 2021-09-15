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

package com.bandyer.video_android_phone_ui.widgets

import android.os.CountDownTimer

/**
 * A composite of Hideable Widgets
 */
class WidgetHidingCoordinator : HideableWidget {

    override var hidingTimer: CountDownTimer? = null

    private var hideableWidgets = mutableListOf<HideableWidget>()

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

    override fun disableAutoHide() {
        hideableWidgets.forEach { it.disableAutoHide() }
    }

    override fun onHidingTimerFinished() {
        hideableWidgets.forEach { it.onHidingTimerFinished() }
    }
}