package com.bandyer.video_android_glass_ui.utils

import android.view.View
import androidx.databinding.BindingAdapter
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.common.AvatarView
import com.bandyer.video_android_glass_ui.common.UserInfoView
import com.bandyer.video_android_glass_ui.common.UserState

object BindingAdapters {
    @BindingAdapter("app:hideIfZero")
    @JvmStatic
    fun hideIfZero(view: View, number: Int) {
        view.visibility = if (number == 0) View.GONE else View.VISIBLE
    }

    // USER INFO VIEW
    @BindingAdapter("app:name")
    @JvmStatic
    fun name(view: UserInfoView, value: String) {
        view.binding.bandyerName.text = value
        with(view.binding.bandyerAvatar) {
            setText(value.first().toString())
            setBackground(value.parseToColor())
        }
    }

    @BindingAdapter("app:state")
    @JvmStatic
    fun state(view: UserInfoView, value: UserState) {
        with(view.binding) {
            bandyerUserStateText.setUserState(value)
            bandyerUserStateDot.isActivated = value == UserState.ONLINE
        }
    }

    @BindingAdapter("app:srcCompat")
    @JvmStatic fun url(view: AvatarView, url: String?) {
        if(url == null) view.setImage(null)
        else view.setImage(url)
    }

    @BindingAdapter("app:hideName")
    @JvmStatic
    fun hideName(view: UserInfoView, value: Boolean) {
        view.binding.bandyerName.visibility = if (value) View.GONE else View.VISIBLE
    }
}
