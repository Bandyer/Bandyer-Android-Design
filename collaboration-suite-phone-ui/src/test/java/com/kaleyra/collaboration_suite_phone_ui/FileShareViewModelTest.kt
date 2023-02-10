package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.viewmodel.FileShareViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class FileShareViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FileShareViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = FileShareViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk()) }
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
    }
}