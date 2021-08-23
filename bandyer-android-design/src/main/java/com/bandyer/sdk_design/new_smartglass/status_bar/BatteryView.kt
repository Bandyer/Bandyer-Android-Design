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
    val spacing = context.dp2px(3f).toFloat()

    // Cap
    private var capPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var capRect = RectF()
    private var capWidthRatio = 0.10f
    private var capHeightRatio = 0.50f
    private var capStroke = context.dp2px(1.5f).toFloat()

    // Body
    private var bodyPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var bodyRect = RectF()
//    private var bodyStrokeWidthRatio = 0.03f
    private var bodyStroke = context.dp2px(3f).toFloat()

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
    private var chargingBitmap = getBitmap(R.drawable.ic_bandyer_sg_charging)

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

        val halfBodyStroke = bodyStroke / 2

        // Cap
        val capMargin = (measureHeight - paddingTop - paddingBottom) * ((1 - capHeightRatio) / 2)
        val capTop = capMargin + paddingTop
        val capBottom = measureHeight - capMargin - paddingBottom
        val capRight = measureWidth - halfBodyStroke - paddingEnd
        val capLeft = measureWidth - measureWidth * capWidthRatio - paddingEnd
        capRect = RectF(capLeft, capTop, capRight, capBottom)

        // Body
        val bodyTop = halfBodyStroke + paddingTop
        val bodyBottom = measureHeight - halfBodyStroke - paddingBottom
        val bodyRight = capLeft - halfBodyStroke - spacing
        val bodyLeft = halfBodyStroke + paddingStart
        bodyRect = RectF(bodyLeft, bodyTop, bodyRight, bodyBottom)

        // Charge
        val chargeTop = bodyStroke + spacing + paddingTop
        val chargeLeft = bodyStroke + spacing + paddingStart
        val chargeBottom = measureHeight - bodyStroke - spacing - paddingBottom
        chargeRectRightMax = bodyRight - spacing - halfBodyStroke
        chargeRect = RectF(chargeLeft, chargeTop, chargeRectRightMax, chargeBottom)

        // Charging Image
        val chargingTop = bodyStroke + spacing + paddingTop
        val chargingBottom = measureHeight - chargingTop - paddingBottom
        val chargingLeft = bodyStroke + spacing + paddingStart
        val chargingRight = bodyRight - chargingLeft - paddingEnd
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
        capPaint.strokeWidth = capStroke
        canvas.drawRect(capRect, capPaint)
    }

    private fun drawBody(canvas: Canvas) {
        bodyPaint.strokeWidth = bodyStroke
        canvas.drawRect(bodyRect, bodyPaint)
    }

    private fun drawCharge(canvas: Canvas, charge: Int) {
        chargePaint.color = if (charge > 25) normalChargeColor else lowChargeColor
        chargeRect.right =
            chargeRect.left + (chargeRectRightMax - chargeRect.left) * charge / 100
        canvas.drawRect(chargeRect, chargePaint)
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
