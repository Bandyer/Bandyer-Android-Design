package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomSheetContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
//            BottomSheetContent()
        }
    }

    // TODO
    // bottom sheet content line must collapsed when needed

   @Test
   fun test1() {

   }

}