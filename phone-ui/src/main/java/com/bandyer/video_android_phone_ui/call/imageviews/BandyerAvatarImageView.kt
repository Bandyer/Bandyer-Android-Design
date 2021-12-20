/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.call.imageviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.bandyer.video_android_phone_ui.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Bandyer avatar image view
 * @property type if single user or multiple placeholder
 * @constructor
 */
class BandyerAvatarImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    private var currentUrl: String? = null
    private var currentUri: Uri? = null

    private var fallbackDrawable: Drawable? = null
    private var fallbackTintList: ColorStateList? = null

    var type: Type? = Type.USER
        set(value) {
            field = value
            currentUri = null
            currentUrl = null
            setImageDrawable(fallbackDrawable)
            imageTintList = fallbackTintList
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        val type = type ?: return drawableState
        return mergeDrawableStates(drawableState, type.value)
    }

    /**
     * Display image given the url
     * @param url image
     * @param preventReload prevent reloading same resource
     */
    fun setImageUrl(url: String, preventReload: Boolean = true) {
        if (preventReload && currentUrl == url) return
        this.currentUrl = url
        Picasso.get().load(url).placeholder(drawable).error(drawable).into(this, object : Callback {
            override fun onSuccess() {
                imageTintList = null
            }

            override fun onError(e: Exception?) = Unit
        })
    }

    /**
     * Display image given the uri
     * @param uri image
     * @param preventReload prevent reloading same resource
     */
    fun setImageUri(uri: Uri, preventReload: Boolean = true) {
        if (preventReload && currentUri == uri) return
        this.currentUri = uri
        Picasso.get().load(uri).placeholder(drawable).error(drawable).into(this, object : Callback {
            override fun onSuccess() {
                imageTintList = null
            }

            override fun onError(e: Exception?) = Unit
        })
    }

    init {
        fallbackDrawable = drawable
        fallbackTintList = imageTintList
    }

    /**
     * Enum representing avatar type
     *
     * @param value state drawable resource
     * @constructor
     */
    enum class Type(val value: IntArray) {

        /**
         * single user
         */
        USER(intArrayOf(R.attr.bandyer_state_user)),

        /**
         * multiple users
         */
        MULTIPLE_USERS(intArrayOf(R.attr.bandyer_state_multiple_users))
    }
}