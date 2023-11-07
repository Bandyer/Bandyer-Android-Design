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

package com.kaleyra.video_glasses_sdk.utils.extensions

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Scroll horizontally the recycler view to the next element. If the next element is not visible, it scrolls by a display width by default.
 *
 * @receiver RecyclerView
 * @param currentIndex The index of the current item
 */
internal fun RecyclerView.horizontalSmoothScrollToNext(currentIndex: Int) {
    if (currentIndex >= adapter!!.itemCount - 1) return
    val target = findViewHolderForAdapterPosition(currentIndex + 1)?.itemView
    if (target == null) smoothScrollBy(context.resources.displayMetrics.widthPixels, 0)
    else scrollToTarget(target)
}

/**
 * Scroll horizontally the recycler view to the previous element. If the previous element is not visible, it scrolls by a negative display width by default.
 *
 * @receiver RecyclerView
 * @param currentIndex The index of the current item
 */
internal fun RecyclerView.horizontalSmoothScrollToPrevious(currentIndex: Int) {
    if (currentIndex <= 0) return
    val target = findViewHolderForAdapterPosition(currentIndex - 1)?.itemView
    if (target == null) smoothScrollBy(-context.resources.displayMetrics.widthPixels, 0)
    else scrollToTarget(target)
}

private fun RecyclerView.scrollToTarget(target: View) {
    val targetX = (target.left + target.right) / 2f
    val half = context.resources.displayMetrics.widthPixels / 2
    smoothScrollBy((targetX - half).toInt(), 0)
}