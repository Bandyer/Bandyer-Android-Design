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

package com.bandyer.video_android_phone_ui.utils

/**
 *
 * Created by federicomarin on 02/12/15.
 */

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan

internal class JumpingSpan : ReplacementSpan() {

    private var translationX = 0f
    private var translationY = 0f

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fontMetricsInt: FontMetricsInt?): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        canvas.drawText(text, start, end, x + translationX, y + translationY, paint)
    }

    fun setTranslationX(translationX: Float) {
        this.translationX = translationX
    }

    fun setTranslationY(translationY: Float) {
        this.translationY = translationY
    }
}