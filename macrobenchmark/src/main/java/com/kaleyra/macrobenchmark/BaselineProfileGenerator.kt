package com.kaleyra.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalBaselineProfilesApi
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun scrollLazyColumn() {
        rule.collectBaselineProfile("com.kaleyra.demo_collaboration_suite_ui") {
            val intent = Intent("$packageName.CHAT_ACTIVITY")
            startActivityAndWait(intent)

            val column = device.findObject(By.res("ConversationTag"))

            val searchCondition = Until.hasObject(By.res("message"))

            column.wait(searchCondition, 5_000)

            column.setGestureMargin(device.displayWidth / 5)

            column.fling(Direction.UP)

            device.waitForIdle()
        }
    }
}