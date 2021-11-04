package com.bandyer.video_android_glass_ui.utils

import android.view.View
import androidx.databinding.BindingAdapter
import com.bandyer.video_android_glass_ui.common.AvatarView

object BindingAdapters {
    @BindingAdapter("app:hideIfZero")
    @JvmStatic
    fun hideIfZero(view: View, number: Int) {
        view.visibility = if (number == 0) View.GONE else View.VISIBLE
    }

    @BindingAdapter("app:srcCompat")
    @JvmStatic fun url(view: AvatarView, url: String?) {
        if(url == null) view.setImage(null)
        else view.setImage(url)
    }
}
