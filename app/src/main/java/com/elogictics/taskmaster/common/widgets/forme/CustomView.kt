package com.elogictics.taskmaster.common.widgets.forme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.LinearLayout
import com.elogictics.taskmaster.R

class CustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var customBackgroundColor: Int = 0
    private var customStrokeColor: Int = 0
    private var customStrokeWidth: Float = 0f
    private var customCornerRadius: Float = 0f
    private var customElevation: Float = 0f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomView)
        customBackgroundColor = typedArray.getColor(R.styleable.CustomView_customBackgroundColor, Color.WHITE)
        customStrokeColor = typedArray.getColor(R.styleable.CustomView_customStrokeColor, Color.BLACK)
        customStrokeWidth = typedArray.getDimension(R.styleable.CustomView_customStrokeWidth, 0f)
        customCornerRadius = typedArray.getDimension(R.styleable.CustomView_customCornerRadius, 0f)

        // Lấy giá trị elevation từ thuộc tính tùy chỉnh
        customElevation = typedArray.getDimension(R.styleable.CustomView_customElevation, 0f)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                // Đặt hình dáng bán kính cho outline
                outline.setRoundRect(0, 0, view.width, view.height, customCornerRadius)
            }
        }

        // Cho phép hiệu ứng shadow
        clipToOutline = true

        typedArray.recycle()
    }

    // Called when the view should render its content.
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // DRAW STUFF HERE
        val backgroundPaint = Paint()
        backgroundPaint.color = customBackgroundColor

        val strokePaint = Paint()
        strokePaint.color = customStrokeColor
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = customStrokeWidth

        // Vẽ một hình chữ nhật bo góc với màu nền, stroke và bán kính bo góc từ thuộc tính tùy chỉnh
        val rectF = RectF(
            customStrokeWidth / 2,
            customStrokeWidth / 2,
            width.toFloat() - customStrokeWidth / 2,
            height.toFloat() - customStrokeWidth / 2
        )

        // Đặt độ nâng (elevation) cho Custom View
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = customElevation
        }

        canvas?.drawRoundRect(rectF, customCornerRadius, customCornerRadius, backgroundPaint)
        canvas?.drawRoundRect(rectF, customCornerRadius, customCornerRadius, strokePaint)
    }
}
