/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_video_sdk.ui.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.kaleyra.video_utils.ContextRetainer.Companion.context
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException

suspend fun Uri.toBitmap(): Bitmap? {
    val imageUrl = toString()
    return when {
        imageUrl.startsWith("http") -> remoteUrlToBitmap(imageUrl)
        imageUrl.startsWith("file") -> localUriToBitmap()
        else                        -> null
    }
}

fun vectorDrawableToBitmap(vectorRes: Int, backgroundColor: Int): Bitmap? {
    val drawable: Drawable = VectorDrawableCompat.create(context.resources, vectorRes, null) ?: return null
    val bitmap = drawable.toBitmap()
    return CircularBitmapTransformation().transform(bitmap, backgroundColor)
}

private fun Uri.localUriToBitmap(): Bitmap? {
    try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(this, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        CircularBitmapTransformation().transform(image)
    } catch (ioe: IOException) {
        ioe.printStackTrace()
    }
    return null
}

private suspend fun remoteUrlToBitmap(url: String): Bitmap? = suspendCancellableCoroutine {
    Picasso.get().load(url).transform(CircularBitmapTransformation()).into(object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            it.resume(bitmap) {}
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {
            it.resume(null) {}
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) = Unit
    })
}

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) return bitmap
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

