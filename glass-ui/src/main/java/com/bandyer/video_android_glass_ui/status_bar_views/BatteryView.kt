package com.bandyer.video_android_glass_ui.status_bar_views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.dp2px
import com.bandyer.video_android_glass_ui.R

/**
 * A custom battery view created using a canvas.
 *
 *  @constructor
 */
internal class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val spacing = context.dp2px(2f).toFloat()

    // Cap
    private var capPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private var capRect = RectF()
    private var capWidthRatio = 0.08f
    private var capHeightRatio = 0.50f
    private var capStroke = context.dp2px(1f).toFloat()

    // Body
    private var bodyPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private var bodyRect = RectF()

    private var bodyStroke = context.dp2px(2f).toFloat()

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
    private var chargingBitmap = getBitmap(R.drawable.ic_bandyer_glass_charging)

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val measureWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(measureWidth, measureHeight)

        val halfBodyStroke = bodyStroke / 2

        // Cap
        val capHorizontalPadding = (measureHeight - paddingTop - paddingBottom) * ((1 - capHeightRatio) / 2)
        val capTop = capHorizontalPadding + paddingTop
        val capBottom = measureHeight - capHorizontalPadding - paddingBottom
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
        chargingRect = RectF(chargeLeft, chargeTop, chargeRectRightMax, chargeBottom)
    }

    override fun onDraw(canvas: Canvas) {
        drawCap(canvas)
        drawBody(canvas)
        if (!isCharging) drawCharge(canvas, charge)
        else drawCharging(canvas)
    }


    /**
     * Update the view's battery charge level
     *
     * @param charge The charge level
     */
    fun setCharge(charge: Int) {
        if (charge > 100 || charge < 0) return
        this.charge = charge
        invalidate()
    }

    /**
     * Update the view's charging state. A lighting in the center of the battery is shows if the value is set to true
     *
     * @param isCharging True to show the lighting, false otherwise
     */
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
        if (chargingBitmap == null) return
        val resizedBitmap = resize(chargingBitmap!!, chargingRect.width(), chargingRect.height())
        val left = chargingRect.left + (chargingRect.width() - resizedBitmap.width) / 2f
        canvas.drawBitmap(resizedBitmap, left, chargingRect.top, null)
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

    private fun resize(bitmap: Bitmap, maxWidth: Float, maxHeight: Float): Bitmap {
        if (maxHeight <= 0 || maxWidth <= 0) return bitmap
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth / maxHeight
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) finalWidth = maxHeight * ratioBitmap
        else finalHeight = maxWidth / ratioBitmap
        return Bitmap.createScaledBitmap(bitmap, finalWidth.toInt(), finalHeight.toInt(), true)
    }
}
