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

package com.bandyer.sdk_design.utils.item_adapter_animators

import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by mikepenz on 08.01.16.
 */
internal open class DefaultAnimator<T> : BaseItemAnimator<T>() {
    // ADD ANIMATION METHODS

    override fun addAnimationPrepare(holder: RecyclerView.ViewHolder) {
        holder.itemView.alpha = 0f
    }

    override fun addAnimation(holder: RecyclerView.ViewHolder): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(holder.itemView).alpha(1f).setDuration(addDuration).setInterpolator(interpolator)
    }

    override fun addAnimationCleanup(holder: RecyclerView.ViewHolder) {
        holder.itemView.alpha = 1f
    }

    // REMOVE ANIMATION METHODS

    override fun removeAnimation(holder: RecyclerView.ViewHolder): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(holder.itemView).setDuration(removeDuration).alpha(0f).setInterpolator(interpolator)
    }

    override fun removeAnimationCleanup(holder: RecyclerView.ViewHolder) {
        holder.itemView.alpha = 1f
    }

    // CHANGE ANIMATION METHODS
    override fun changeOldAnimation(holder: RecyclerView.ViewHolder, changeInfo: ChangeInfo): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(holder.itemView).setDuration(changeDuration).alpha(0f).translationX((changeInfo.toX - changeInfo.fromX).toFloat()).translationY((changeInfo.toY - changeInfo.fromY).toFloat()).setInterpolator(interpolator)
    }

    override fun changeNewAnimation(holder: RecyclerView.ViewHolder): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(holder.itemView).translationX(0f).translationY(0f).setDuration(changeDuration).alpha(1f).setInterpolator(interpolator)
    }

    override fun changeAnimationCleanup(holder: RecyclerView.ViewHolder?) {
        holder!!.itemView.alpha = 1f
    }
}