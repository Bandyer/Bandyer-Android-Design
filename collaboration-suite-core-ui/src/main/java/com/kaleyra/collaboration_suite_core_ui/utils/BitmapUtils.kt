package com.kaleyra.collaboration_suite_core_ui.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

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
}