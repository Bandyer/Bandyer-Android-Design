package com.kaleyra.macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 */
@RunWith(AndroidJUnit4::class)
class ChatScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun chatScrollNoCompilation() = chatScroll(CompilationMode.None())

    @Test
    fun chatScrollBaselineProfile() = chatScroll(
        CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        )
    )

    private fun chatScroll(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = "com.kaleyra.demo_collaboration_suite_ui",
            metrics = listOf(FrameTimingMetric()),
            iterations = 10,
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            setupBlock = {
                val intent = Intent("$packageName.CHAT_ACTIVITY")
                startActivityAndWait(intent)
            }
        ) {
            val column = device.findObject(By.res("lazyColumnMessages"))

            val searchCondition = Until.hasObject(By.res("message"))

            column.wait(searchCondition, 5_000)

            column.setGestureMargin(device.displayWidth / 5)

            repeat(5) {
                column.fling(Direction.UP)
            }

            device.waitForIdle()
        }
    }
}