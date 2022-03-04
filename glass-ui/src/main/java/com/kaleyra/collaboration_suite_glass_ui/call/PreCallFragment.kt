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

package com.kaleyra.collaboration_suite_glass_ui.call

import android.annotation.SuppressLint
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.HorizontalAutoScrollView

internal abstract class PreCallFragment : ConnectingFragment(), HorizontalAutoScrollView.OnScrollListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceBound() {
        super.onServiceBound()

        with(binding) {
            bandyerParticipantsScrollView.onScrollListener = this@PreCallFragment
            root.setOnTouchListener { _, event -> bandyerParticipantsScrollView.onTouchEvent(event) }

            repeatOnStarted {
                viewModel.call.participants.onEach { participants ->
                    bandyerCounter.text = resources.getString(
                        R.string.bandyer_glass_n_of_participants_pattern,
                        participants.others.size + 1
                    )

                    val userAliases = participants.others.plus(participants.me).map { it.userAlias }
                    bandyerParticipants.text = viewModel.usersDescription.name(userAliases)
                    updateUIOnParticipantsViewChange()

                    setSubtitle(participants.others.count() + 1 > 2)
                }.launchIn(this@repeatOnStarted)
            }
        }
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        updateUIOnParticipantsViewChange()
//    }

    private fun updateUIOnParticipantsViewChange() = with(binding) {
        bandyerParticipants.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val hideProgressUI =
            resources.displayMetrics.widthPixels - bandyerParticipants.measuredWidth > 0
        bandyerBottomNavigation.apply { if (hideProgressUI) hideSwipeHorizontalItem() else showSwipeHorizontalItem() }
        bandyerProgress.visibility = if (hideProgressUI) View.GONE else View.VISIBLE
    }

    override fun onScrollChanged(x: Int, y: Int): Unit =
        with(binding) {
            bandyerProgress.apply {
                max = bandyerParticipantsScrollView.getChildAt(0).width
                progress = ((x + bandyerParticipantsScrollView.width).toFloat()).roundToInt()
            }
        }

}