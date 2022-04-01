package com.kaleyra.collaboration_suite_core_ui.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.Px
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

object BitmapUtils {
    fun roundBitmap(bitmap: Bitmap): Bitmap {
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(newBitmap)
        val path = Path().apply {
            val halfWidth = bitmap.width / 2f
            val halfHeight = bitmap.width / 2f
            addCircle(halfWidth, halfHeight, halfWidth, Path.Direction.CW)
            toggleInverseFillType()
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        canvas.drawPath(path, paint)
        return newBitmap
    }

//    suspend fun uriToBitmap(uri: Uri, @Px width: Int, @Px height: Int) =
//        suspendCancellableCoroutine<Bitmap?> { continuation ->
//            Picasso.get()
//                .load(uri)
//                .resize(500, 500)
//                .centerCrop()
//                .into(object : Target {
//                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit
//
//                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) =
//                        continuation.resume(bitmap, null)
//
//                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) =
//                        continuation.resume(null, null)
//                })
//        }

    fun uriToBitmap(
        uri: Uri,
        @Px width: Int,
        @Px height: Int,
        onSuccess: (Bitmap?) -> Unit,
        onFailure: () -> Unit
    ) {
        Picasso.get()
            .load(uri)
            .resize(width, height)
            .centerCrop()
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) = Unit

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) =
                    onSuccess.invoke(bitmap)

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) =
                    onFailure.invoke()
            })
    }
}