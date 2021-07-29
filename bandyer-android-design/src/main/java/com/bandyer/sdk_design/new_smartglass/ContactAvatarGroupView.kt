package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerContactGroupLayoutBinding

class ContactAvatarGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: BandyerContactGroupLayoutBinding =
        BandyerContactGroupLayoutBinding.inflate(
            LayoutInflater.from(context), this, true
        )

    init {

    }

    fun setFirstAvatar(@DrawableRes resId: Int?) = binding.bandyerFirstAvatar.setAvatar(resId)

    fun setFirstAvatarLetter(text: String?) = binding.bandyerFirstAvatar.setText(text)

    fun setSecondAvatar(@DrawableRes resId: Int?) = binding.bandyerSecondAvatar.setAvatar(resId)

    fun setSecondAvatarLetter(text: String?) = binding.bandyerSecondAvatar.setText(text)

    fun setGroupedAvatarsNumber(number: Int?) = binding.bandyerFirstAvatar.setText(number?.let {
        resources.getString(
            R.string.bandyer_smartglass_group_contacts_pattern, it
        )
    })
}