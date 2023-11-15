package com.kaleyra.demo_video_sdk.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader.TileMode.CLAMP
import com.squareup.picasso.Transformation

class CircularBitmapTransformation : Transformation {
    private var backgroundColor = Color.TRANSPARENT
    fun transform(source: Bitmap, backgroundColor: Int): Bitmap {
        this.backgroundColor = backgroundColor
        return transform(source)
    }

    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) source.recycle()
        val bitmap = Bitmap.createBitmap(size, size, source.config)
        val canvas = Canvas(bitmap)
        val backgroundPaint = Paint()
        backgroundPaint.color = backgroundColor
        val paint = Paint()
        val shader = BitmapShader(squaredBitmap, CLAMP, CLAMP)
        paint.shader = shader
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, backgroundPaint)
        canvas.drawCircle(r, r, r, paint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "CircularBitmapTransformation"
    }
}