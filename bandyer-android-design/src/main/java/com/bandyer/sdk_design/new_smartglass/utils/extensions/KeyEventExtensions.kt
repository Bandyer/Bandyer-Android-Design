package com.bandyer.sdk_design.new_smartglass.utils.extensions

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
 * Checks if keyEvent is a dpad down button
 * @receiver KeyEvent
 * @return true if is down dpad button else otherwise
 */
internal fun KeyEvent.isConfirmButton(): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE