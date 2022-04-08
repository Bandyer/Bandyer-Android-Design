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

package com.kaleyra.collaboration_suite_glass_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_glass_ui.model.internal.StreamParticipant
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue

class ViewModelTest {

    @ExperimentalCoroutinesApi
    @Test
    fun toParticipant_assert() = runTest {
        val participantsFlow = MutableSharedFlow<CallParticipants>()
        val callParticipants = MockCallParticipants()

        val expected = callParticipants.others
            .plus(callParticipants.me)
            .map { Participant(it, itsMe = it == callParticipants.me) }
        val result = mutableListOf<Participant>()
        participantsFlow
            .toParticipant()
            .take(expected.size)
            .onEach { result.add(it) }
            .onCompletion { assertEquals(result, expected) }
            .launchIn(this)

        launch { participantsFlow.emit(callParticipants) }

        advanceUntilIdle()
    }
}

data class Participant(val participant: CallParticipant, val itsMe: Boolean)

fun Flow<CallParticipants>.toParticipant(): Flow<Participant> =
    transform {
        val participants = it.others.plus(it.me)
        participants.forEach { p -> emit(Participant(p, itsMe = p == it.me)) }
    }

//internal fun Flow<CallParticipants>.toStreamParticipants(scope: CoroutineScope): Flow<List<StreamParticipant>> {
//    val uiStreams = ConcurrentLinkedQueue<StreamParticipant>()
//
//    this.toParticipant()
//        .onEach { participant ->
//            combine(participant.streams, participant.state) { streams, state ->
//                if (participant == me || (state == CallParticipant.State.IN_CALL && streams.isNotEmpty())) {
//                    val newStreams = streams.map {
//                        StreamParticipant(
//                            participant,
//                            itsMe,
//                            it,
//                            usersDescription.name(listOf(participant.userId)),
//                            usersDescription.image(listOf(participant.userId))
//                        )
//                    }
//                    val currentStreams = uiStreams.filter { it.participant == participant }
//                    val addedStreams = newStreams - currentStreams.toSet()
//                    val removedStreams = currentStreams - newStreams.toSet()
//                    uiStreams += addedStreams
//                    uiStreams -= removedStreams.toSet()
//
//                    removedStreams.map { it.stream.id }.forEach { _removedStreams.emit(it) }
//                } else {
//                    uiStreams
//                        .filter { it.participant == participant }
//                        .map { it.stream.id }
//                        .forEach { _removedStreams.emit(it) }
//                    uiStreams.removeAll { it.participant == participant }
//                }
//                emit(uiStreams.toList())
//            }.launchIn(scope)
//        }.launchIn(scope)
//}