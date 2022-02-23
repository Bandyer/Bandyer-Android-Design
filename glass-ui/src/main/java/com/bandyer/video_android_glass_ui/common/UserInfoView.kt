package com.bandyer.video_android_glass_ui.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassUserInfoLayoutBinding
import com.bandyer.video_android_glass_ui.model.internal.UserState

internal class UserInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = BandyerGlassUserInfoLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun setName(name: String) { binding.bandyerName.text = name }

    fun setAvatarBackgroundAndLetter(name: String) = with(binding) {
        bandyerAvatar.setText(name.first().toString())
        bandyerAvatar.setBackground(name.parseToColor())
    }

//    fun setAvatar(url: String) = binding.bandyerAvatar.setImage(url)

    fun setAvatar(uri: Uri) = binding.bandyerAvatar.setImage(uri)

    fun setAvatar(@DrawableRes resId: Int?) = binding.bandyerAvatar.setImage(resId)

    fun setState(state: UserState, lastSeenTime: Long = 0) = with(binding) {
        if(state is UserState.Offline) bandyerUserStateText.setUserState(state, lastSeenTime)
        else bandyerUserStateText.setUserState(state)
        bandyerUserStateDot.isActivated = state == UserState.Online
    }

    fun hideName(value: Boolean) { binding.bandyerName.visibility = if (value) View.GONE else View.VISIBLE }
}