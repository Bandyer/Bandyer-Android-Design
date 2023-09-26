package com.kaleyra.collaboration_suite_phone_ui.ui.call.snackbar

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.common.snackbar.UserMessageSnackbar
import com.kaleyra.collaboration_suite_phone_ui.ui.performHorizontalSwipe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserMessageSnackbarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSwipeToDismiss() {
        composeTestRule.setContent {
            UserMessageSnackbar(
                iconPainter =  painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
                title = "title"
            )
        }
        composeTestRule.onNodeWithText("title").assertIsDisplayed()
        composeTestRule.onNodeWithText("title").onParent().performHorizontalSwipe(8f)
        composeTestRule.onNodeWithText("title").assertIsNotDisplayed()
    }
}