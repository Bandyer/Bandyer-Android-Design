package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.viewmodel.DialingViewModel
import io.mockk.mockk
import io.mockk.spyk
import org.junit.After
import org.junit.Before

internal class DialingViewModelTest: PreCallViewModelTest<DialingViewModel, DialingUiState>() {

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = spyk(DialingViewModel { Configuration.Success(conferenceMock, mockk(), companyMock) })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }
}