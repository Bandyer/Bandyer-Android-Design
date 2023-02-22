package com.kaleyra.collaboration_suite_core_ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
object TestHelper {

    fun MainDispatcherRule.stopCollectingOnDispatcher() {
        testDispatcher.cancelChildren()
    }


    fun TestScope.stopCollecting() {
        this.coroutineContext.cancelChildren()
    }
}