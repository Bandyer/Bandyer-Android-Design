package com.kaleyra.collaboration_suite_glass_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.call.CallUIDelegate
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.SharedFlow

class MockCallUIDelegate(
    override val call: SharedFlow<Call>,
    override val usersDescription: UsersDescription
) : CallUIDelegate {
}