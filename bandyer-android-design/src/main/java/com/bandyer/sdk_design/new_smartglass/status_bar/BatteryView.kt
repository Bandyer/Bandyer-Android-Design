package com.bandyer.sdk_design.new_smartglass.status_bar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.PaintDrawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.toRect
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.dp2px

class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var radius = context.dp2px(3f).toFloat()
    private var spacingRatio = 0.04f

    // Cap
    private var capPaint =
        PaintDrawable(Color.WHITE) // It allows to round only some corners
    private var capRect = RectF()
    private var capWidthRatio = 0.05f
    private var capHeightRatio = 0.33f

    // Body
    private var bodyPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var bodyRect = RectF()
    private var bodyStrokeWidthRatio = 0.03f
    private var bodyStroke = 0f

    // Charge
    private var chargePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }
    private var chargeRect = RectF()
    private var chargeRectRightMax = 0f
    private var charge = 0
    private var isCharging = false
    private var normalChargeColor = Color.WHITE
    private var lowChargeColor = Color.RED

    // Charging
    private var chargingRect = RectF()
    private var chargingBitmap = getBitmap(R.drawable.ic_bandyer_charging)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.BatteryView).apply {
            charge = getInt(R.styleable.BatteryView_bandyer_charge, 0)
            isCharging = getBoolean(R.styleable.BatteryView_bandyer_isCharging, false)
            recycle()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val measureWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)

        bodyStroke = bodyStrokeWidthRatio * measureWidth
        val halfBodyStroke = bodyStroke / 2
        val spacing = measureWidth * spacingRatio

        // Cap
        val capTop = measureHeight * (1 - capHeightRatio) / 2
        val capBottom = measureHeight - capTop
        val capRight = measureWidth - halfBodyStroke
        val capLeft = measureWidth - measureWidth * capWidthRatio
        capRect = RectF(capLeft, capTop, capRight, capBottom)

        // Body
        val bodyBottom = measureHeight - halfBodyStroke
        val bodyRight = capLeft - halfBodyStroke - spacing
        bodyRect = RectF(halfBodyStroke, halfBodyStroke, bodyRight, bodyBottom)

        // Charge
        val chargeTopLeft = bodyStroke + spacing
        val chargeBottom = measureHeight - chargeTopLeft
        chargeRectRightMax = bodyRight - spacing - halfBodyStroke
        chargeRect = RectF(chargeTopLeft, chargeTopLeft, chargeRectRightMax, chargeBottom)

        // Charging Image
        val chargingTop = bodyStroke + spacing
        val chargingBottom = measureHeight - chargingTop
        val chargingLeft = bodyStroke + spacing
        val chargingRight = bodyRight - chargingLeft
        chargingRect = RectF(chargingLeft, chargingTop, chargingRight, chargingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        drawCap(canvas)
        drawBody(canvas)
        if (!isCharging) drawCharge(canvas, charge)
        else drawCharging(canvas)
    }

    fun setCharge(charge: Int) {
        if (charge > 100 || charge < 0) return
        this.charge = charge
        invalidate()
    }

    fun setCharging(isCharging: Boolean) {
        this.isCharging = isCharging
        invalidate()
    }

    private fun drawCap(canvas: Canvas) {
        capPaint.bounds = capRect.toRect()
        capPaint.setCornerRadii(floatArrayOf(0f, 0f, 50f, 50f, 50f, 50f, 0f, 0f))
        capPaint.draw(canvas)
    }

    private fun drawBody(canvas: Canvas) {
        bodyPaint.strokeWidth = bodyStroke
        canvas.drawRoundRect(bodyRect, radius, radius, bodyPaint)
    }

    private fun drawCharge(canvas: Canvas, charge: Int) {
        chargePaint.color = if (charge > 25) normalChargeColor else lowChargeColor
        chargeRect.right =
            chargeRect.left + (chargeRectRightMax - chargeRect.left) * charge / 100
        canvas.drawRoundRect(chargeRect, radius, radius, chargePaint)
    }

    private fun drawCharging(canvas: Canvas) {
        if(chargingBitmap == null) return
        canvas.drawBitmap(chargingBitmap!!, null, chargingRect, chargePaint)
    }

    private fun getBitmap(
        @DrawableRes drawableId: Int,
    ): Bitmap? {
        val drawable = AppCompatResources.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
