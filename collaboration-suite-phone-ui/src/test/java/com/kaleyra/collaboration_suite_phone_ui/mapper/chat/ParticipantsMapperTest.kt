package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import android.net.Uri
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.Mocks
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatInfo
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ParticipantsMapperTest {

    @Test
    fun usersDetails_getChatInfo_userIdAndImageUri() = runTest {
        mockkObject(ContactDetailsManager)
        val uriMock = mockk<Uri>()
        every { Mocks.otherParticipantMock.combinedDisplayName } returns MutableStateFlow("customDisplayName")
        every { Mocks.otherParticipantMock.combinedDisplayImage } returns MutableStateFlow(uriMock)
        Assert.assertEquals(flowOf(Mocks.chatParticipantsMock).toChatInfo().first(), ChatInfo("customDisplayName", ImmutableUri(uriMock)))
    }
}