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

package com.kaleyra.video_sdk.call.mapper

import android.net.Uri
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.utils.FlowUtils.flatMapLatestNotNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transform

object ParticipantMapper {

    fun Flow<Call>.isMeParticipantInitialized(): Flow<Boolean> =
        flatMapLatest { it.participants }.map { it.me != null }

    fun Flow<Call>.isGroupCall(companyId: Flow<String>): Flow<Boolean> =
        combine(this.flatMapLatest { it.participants }, companyId) { participants, companyId ->
            participants.others.filter { it.userId != companyId }.size > 1
        }.distinctUntilChanged()

    fun Flow<Call>.toOtherDisplayNames(): Flow<List<String>> =
        this.flatMapLatest { it.participants }
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, String?>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.combinedDisplayName.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayName) ->
                        map[userId] = displayName
                        val values = map.values.toList().filterNotNull()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
            .distinctUntilChanged()

    fun Flow<Call>.toOtherDisplayImages(): Flow<List<Uri>> =
        this.flatMapLatest { it.participants }
            .flatMapLatest { participants ->
                val others = participants.others
                val map = mutableMapOf<String, Uri?>()

                if (others.isEmpty()) flowOf(listOf())
                else others
                    .map { participant ->
                        participant.combinedDisplayImage.map { displayName ->
                            Pair(participant.userId, displayName)
                        }
                    }
                    .merge()
                    .transform { (userId, displayImage) ->
                        map[userId] = displayImage
                        val values = map.values.toList().filterNotNull()
                        if (values.size == others.size) {
                            emit(values)
                        }
                    }
            }
            .distinctUntilChanged()

    fun Flow<Call>.toMyParticipantState(): Flow<CallParticipant.State> =
        this.flatMapLatest { it.participants }
            .flatMapLatestNotNull { it.me?.state }
            .distinctUntilChanged()
}