/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.utils.extensions

import android.view.KeyEvent

/**
 * Returns true if a keyEvent was generated from Dpad.
 * @receiver KeyEvent
 * @return Boolean
 */
internal fun KeyEvent.isDpad(): Boolean {
    return keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_ENTER ||
            keyCode == KeyEvent.KEYCODE_SPACE ||
            keyCode == KeyEvent.KEYCODE_TAB
}

/**
 * Checks if keyEvent is a dpad left button
 * @receiver KeyEvent
 * @return true if is left dpad button else otherwise
 */
internal fun KeyEvent.isLeftButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_LEFT

/**
 * Checks if keyEvent is a dpad up button
 * @receiver KeyEvent
 * @return true if is up dpad button else otherwise
 */
internal fun KeyEvent.isUpButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_UP

/**
 * Checks if keyEvent is a dpad right button
 * @receiver KeyEvent
 * @return true if is right dpad button else otherwise
 */
internal fun KeyEvent.isRightButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT

/**
 * Checks if keyEvent is a dpad down button
 * @receiver KeyEvent
 * @return true if is down dpad button else otherwise
 */
internal fun KeyEvent.isDownButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_DOWN

/**
 * Checks if keyEvent is a tab key
 * @receiver KeyEvent
 * @return true if is tab key else otherwise
 */
internal fun KeyEvent.isTabKey(): Boolean = keyCode == KeyEvent.KEYCODE_TAB

/**
 * Checks if keyEvent is a shift left key
 * @receiver KeyEvent
 * @return true if is shift left key else otherwise
 */
internal fun KeyEvent.isShiftLeftKey(): Boolean = keyCode == KeyEvent.KEYCODE_SHIFT_LEFT

/**
 * Checks if keyEvent is a dpad down button
 * @receiver KeyEvent
 * @return true if is down dpad button else otherwise
 */
internal fun KeyEvent.isConfirmButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE