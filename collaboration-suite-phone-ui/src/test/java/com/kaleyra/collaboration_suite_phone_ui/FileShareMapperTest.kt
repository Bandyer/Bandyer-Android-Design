package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class FileShareMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

}