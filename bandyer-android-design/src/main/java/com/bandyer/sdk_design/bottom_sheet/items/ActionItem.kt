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

package com.bandyer.sdk_design.bottom_sheet.items

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes

/**
 *  Generic item used for the bottom sheet
 *
 * @property viewId id of the view to inflate
 * @property viewLayoutRes layout to inflate
 * @property viewStyle style of the view
 * @constructor Create empty Action item
 */
abstract class ActionItem(@IdRes var viewId: Int, @LayoutRes var viewLayoutRes: Int = 0, @StyleRes var viewStyle: Int = 0) {

    /**
     * Action item view
     */
    var itemView: View? = null

    /**
     * Indicates whether some other object is "equal to" this one. Implementations must fulfil the following
     * requirements:
     *
     * * Reflexive: for any non-null reference value x, x.equals(x) should return true.
     * * Symmetric: for any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) returns true.
     * * Transitive:  for any non-null reference values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, then x.equals(z) should return true
     * * Consistent:  for any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true or consistently return false, provided no information used in equals comparisons on the objects is modified.
     *
     * Note that the `==` operator in Kotlin code is translated into a call to [equals] when objects on both sides of the
     * operator are not null.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActionItem
        if (viewId != other.viewId) return false
        return true
    }

    /**
     * Returns a hash code value for the object.  The general contract of hashCode is:
     *
     * * Whenever it is invoked on the same object more than once, the hashCode method must consistently return the same integer, provided no information used in equals comparisons on the object is modified.
     * * If two objects are equal according to the equals() method, then calling the hashCode method on each of the two objects must produce the same integer result.
     */
    override fun hashCode(): Int = viewId

    /**
     * Method called when the layout has been inflated and binded
     */
    abstract fun onReady()
}