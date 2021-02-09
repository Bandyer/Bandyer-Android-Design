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

package com.bandyer.sdk_design.drawables


import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.Style.STROKE
import android.graphics.PathDashPathEffect.Style.ROTATE
import android.graphics.drawable.Drawable
import android.util.Property


/**
 * @suppress
 * Red Bordered rectangle drawable animated
 * @property width Int
 * @property height Int
 * @property linePaint Paint
 * @property length Float
 * @property dotPaint Paint
 * @property rectPath Path
 * @property pathDot Path
 * @property initialPhase Float
 * @constructor
 */
class AnimatedRedBorderDrawable(borderSize: Float, val width: Int, val height: Int) : Drawable() {
    var dotProgress = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            callback?.invalidateDrawable(this)
        }

    private val linePaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = STROKE
        strokeWidth = borderSize
        color = Color.RED
    }

    private val length by lazy {
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(rectPath, false)
        pathMeasure.length
    }

    private val dotPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = STROKE
        strokeWidth = borderSize
        shader = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.RED, Color.parseColor("#8B0000"), Shader.TileMode.CLAMP)
    }

    private val rectPath = Path().apply {
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        addRect(rect, Path.Direction.CW)
    }

    private val pathDot = Path().apply {
        val rect = RectF(0f, 0f, length, borderSize)
        addRect(rect, Path.Direction.CW)
    }

    private val initialPhase by lazy(LazyThreadSafetyMode.NONE) {
        (1f - (1f / (2 * 1))) * length
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(rectPath, linePaint)

        val phase = initialPhase + dotProgress * length * 1
        dotPaint.pathEffect = PathDashPathEffect(pathDot, length, phase, ROTATE)
        canvas.drawPath(rectPath, dotPaint)
    }

    override fun setAlpha(alpha: Int) {
        linePaint.alpha = alpha
        dotPaint.alpha = alpha
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        linePaint.colorFilter = colorFilter
        dotPaint.colorFilter = colorFilter
    }

    override fun getIntrinsicWidth() = width

    override fun getIntrinsicHeight() = height

    object DOT_PROGRESS : Property<AnimatedRedBorderDrawable, Float>(Float::class.java, "dotProgress") {
        override fun set(drawable: AnimatedRedBorderDrawable, dotProgress: Float) {
            drawable.dotProgress = dotProgress
        }

        override fun get(drawable: AnimatedRedBorderDrawable) = drawable.dotProgress
    }
}