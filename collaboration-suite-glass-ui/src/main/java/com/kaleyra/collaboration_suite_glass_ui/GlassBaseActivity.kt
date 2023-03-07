package com.kaleyra.collaboration_suite_glass_ui

import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.MotionEvent
import com.kaleyra.collaboration_suite_core_ui.CollaborationActivity
import com.kaleyra.collaboration_suite_glass_ui.common.OnDestinationChangedListener
import com.kaleyra.collaboration_suite_glass_ui.utils.currentNavigationFragment

/**
 * GlassBaseActivity
 */
internal abstract class GlassBaseActivity : CollaborationActivity(), OnDestinationChangedListener, GlassTouchEventManager.Listener {

    private var glassTouchEventManager: GlassTouchEventManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glassTouchEventManager = GlassTouchEventManager(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        glassTouchEventManager = null
    }

    /**
     * @suppress
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(ev)) true
        else super.dispatchTouchEvent(ev)

    /**
     * @suppress
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (glassTouchEventManager!!.toGlassTouchEvent(event)) true
        else super.dispatchKeyEvent(event)

    override fun onGlassTouchEvent(glassEvent: TouchEvent): Boolean {
        val currentDest = supportFragmentManager.currentNavigationFragment as? TouchEventListener ?: return false
        return currentDest.onTouch(glassEvent)
    }

}