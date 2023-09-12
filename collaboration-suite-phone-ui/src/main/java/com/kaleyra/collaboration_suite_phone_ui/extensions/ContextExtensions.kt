package com.kaleyra.collaboration_suite_phone_ui.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

internal object ContextExtensions {
    /**
     * Find the closest Activity in a given Context.
     */
    fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        throw IllegalStateException("Permissions should be called in the context of an Activity")
    }
}