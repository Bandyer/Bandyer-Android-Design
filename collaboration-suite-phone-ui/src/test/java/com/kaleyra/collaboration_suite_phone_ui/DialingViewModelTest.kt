package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.viewmodel.DialingViewModel
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before

internal class DialingViewModelTest: PreCallViewModelTest<DialingViewModel, DialingUiState>() {

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = spyk(DialingViewModel { Configuration.Success(phoneBoxMock, mockk(), MutableStateFlow(companyNameMock), MutableStateFlow(themeMock)) })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }
}