package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.dialing.viewmodel.DialingViewModel
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before

internal class DialingViewModelTest: PreCallViewModelTest<DialingViewModel, DialingUiState>() {

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = spyk(DialingViewModel { Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }
}