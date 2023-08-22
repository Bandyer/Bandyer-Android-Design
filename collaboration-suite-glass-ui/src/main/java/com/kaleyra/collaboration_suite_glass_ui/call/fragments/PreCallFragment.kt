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

package com.kaleyra.collaboration_suite_glass_ui.call.fragments

import android.annotation.SuppressLint
import android.view.View
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.HorizontalAutoScrollView
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

internal abstract class PreCallFragment : ConnectingFragment(),
    HorizontalAutoScrollView.OnScrollListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun bindUI() {
        super.bindUI()

        with(binding) {
            kaleyraParticipantsScrollView.onScrollListener = this@PreCallFragment
            root.setOnTouchListener { _, event -> kaleyraParticipantsScrollView.onTouchEvent(event) }

            repeatOnStarted {
                viewModel.participants.onEach { participants ->
                    kaleyraCounter.text = resources.getString(
                        R.string.kaleyra_glass_n_of_participants_pattern,
                        participants.others.size + 1
                    )

                    val nParticipants = participants.others.count() + 1
                    val userIds =
                        participants.others.let { if (nParticipants > 2) it.plus(participants.me) else it }.map { it.userId }
                    kaleyraParticipants.text = viewModel.usersDescription.name(userIds)
                    updateUIOnParticipantsViewChange()

                    setSubtitle(nParticipants > 2, viewModel.call.replayCache.first().isLink)
                }.launchIn(this@repeatOnStarted)
            }
        }
    }

    override fun onDestroyView() {
        binding.kaleyraParticipantsScrollView.onScrollListener = null
        super.onDestroyView()
    }

    private fun updateUIOnParticipantsViewChange() = with(binding) {
        kaleyraParticipants.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val hideProgressUI =
            resources.displayMetrics.widthPixels - kaleyraParticipants.measuredWidth > 0
        kaleyraBottomNavigation.apply { if (hideProgressUI) hideFirstItem() else showFirstItem() }
        kaleyraProgress.visibility = if (hideProgressUI) View.GONE else View.VISIBLE
    }

    override fun onScrollChanged(x: Int, y: Int): Unit =
        with(binding) {
            kaleyraProgress.apply {
                max = kaleyraParticipantsScrollView.getChildAt(0).width
                progress = ((x + kaleyraParticipantsScrollView.width).toFloat()).roundToInt()
            }
        }

}