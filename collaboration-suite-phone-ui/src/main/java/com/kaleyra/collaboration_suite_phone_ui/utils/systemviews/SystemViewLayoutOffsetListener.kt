/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.utils.systemviews

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.implementation.SystemViewControlsAware
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.implementation.SystemViewControlsAwareInstance

/**
 * Component to use when you want to know if any system view has changed been drawn.
 * Useful to know the available space where we want to draw in cases like a system overlay.
 *
 * The basic usage is:
 *
 * SystemViewLayoutOffsetListener.addObserver(context,this)
 *
 * as soon the view/layout has been drawn or attached call this method
 * SystemViewLayoutOffsetListener.getOffsets(context)
 *
 * When finished call
 * SystemViewLayoutOffsetListener.removeObserver(context,this)
 *
 *
 * @author kristiyan
 */
interface SystemViewLayoutOffsetListener {

    /**
     * SystemViewLayoutOffsetListener Instance
     */
    companion object Instance {

        private val systemViewControlsAwareInstances = hashMapOf<FragmentActivity, SystemViewControlsAwareInstance>()

        /**
         * Add an observer to the system controls changes
         * @param observer SystemViewLayoutObserver
         */
        fun addObserver(mContext: FragmentActivity, observer: SystemViewLayoutObserver, removeOnInsetChanged: Boolean = false) {
            systemViewControlsAwareInstances[mContext] = getOrCreate(mContext)
            systemViewControlsAwareInstances[mContext]!!.addObserver(observer, removeOnInsetChanged)
        }

        /**
         * Remove the observer from the SystemViewControlsAware
         * @param mContext the context where the observer will be binded to
         * @param observer SystemViewLayoutObserver
         */
        fun removeObserver(mContext: FragmentActivity, observer: SystemViewLayoutObserver) {
            get(mContext)?.removeObserver(observer)
        }

        /**
         * @param mContext the context requiring the offsets
         * Request to calculate the offsets
         */
        fun getOffsets(mContext: FragmentActivity) {
            get(mContext)?.getOffsets()
        }

        private fun getOrCreate(mContext: FragmentActivity): SystemViewControlsAwareInstance {
            if (systemViewControlsAwareInstances.containsKey(mContext))
                return systemViewControlsAwareInstances[mContext]!!
            return SystemViewControlsAware {
                systemViewControlsAwareInstances.remove(mContext)
            }.bind(mContext)
        }

        private fun get(mContext: FragmentActivity): SystemViewControlsAwareInstance? {
            return systemViewControlsAwareInstances[mContext]
        }
    }
}