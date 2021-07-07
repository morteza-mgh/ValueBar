package mgh.morteza.valuebarselector

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.math.max


class ValueBar : View {
    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context = context, attrs = attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context = context, attrs = attrs, defStyleAttr = defStyleAttr)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(
            context = context,
            attrs = attrs,
            defStyleAttr = defStyleAttr,
            defStyleRes = defStyleRes
        )
    }


    var isFirstValidate = true

    var selectorColor = Color.parseColor("#eeffb11a") /////////
    var bgValueBarColor = Color.WHITE ////////
    private var crossLineColor = Color.parseColor("#998E8E8E")
    var minValue = 0 ////////
    var maxValue = 100 /////
    var currentValue = 0 ////////

    var unitText = "" ////////


    var barThicknessSize = 45 ////////
    private var crossLineThickness = 3
    private val lineNumber = 35
    private var progressLineThickness = 12
    private var scrollThumbThickness = 30
    private var popMsgThickness = 0
    private var popMsgTextSize = 24
    private var spaceAfterPopMsg = 30 //will be in dp


    private lateinit var rectPaint: Paint
    private lateinit var selectorPaint: Paint
    private lateinit var crossLinePaint: Paint
    private lateinit var popMsgPaint: Paint

    private var rectFValueBar = RectF()


    private var barHeight = 0
    private var topEdge = 20
    private var bottomEdge = 20
    private var xTranslationToCenterValueBar = 0
    private var yTranslationToBottomValueBar = 0f
    private var yCenterBar = 0
    private var density = 1f

    private var onProgressChangeListener: OnProgressChangeListener? = null

    interface OnProgressChangeListener {
        fun onChange(progress: Float)
    }

    private fun init(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        density = context?.resources?.displayMetrics?.density!!

        val ta: TypedArray? = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.ValueBar,
            defStyleAttr,
            defStyleRes
        )
        ta?.let {

            selectorColor = it.getColor(R.styleable.ValueBar_selectorColor, selectorColor)
            bgValueBarColor = it.getColor(R.styleable.ValueBar_bgValueBarColor, bgValueBarColor)
            crossLineColor = it.getColor(R.styleable.ValueBar_crossLineColor, crossLineColor)

            minValue = it.getInteger(R.styleable.ValueBar_min, minValue)
            maxValue = it.getInteger(R.styleable.ValueBar_max, maxValue)
            currentValue = it.getInteger(R.styleable.ValueBar_progress, (minValue+maxValue)/2)
            unitText = it.getString(R.styleable.ValueBar_unitNameLabel) ?: unitText
            popMsgTextSize = it.getDimensionPixelSize(
                R.styleable.ValueBar_popMsgTextSize,
                popMsgTextSize * density.toInt()
            )

            barThicknessSize =
                it.getDimensionPixelSize(
                    R.styleable.ValueBar_barThicknessSize,
                    barThicknessSize * density.toInt()
                )

            it.recycle()
        }

        spaceAfterPopMsg *= density.toInt()
        crossLineThickness *= density.toInt()
        progressLineThickness = barThicknessSize / 15
        scrollThumbThickness = barThicknessSize / 2

        rectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        rectPaint.color = bgValueBarColor

        selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        selectorPaint.color = selectorColor


        crossLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        crossLinePaint.textAlign = Paint.Align.CENTER
        crossLinePaint.color = crossLineColor

        popMsgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        popMsgPaint.textAlign = Paint.Align.CENTER
        popMsgPaint.color = Color.WHITE
        popMsgPaint.typeface = Typeface.DEFAULT
        popMsgPaint.textSize = popMsgTextSize.toFloat()


    }

    private fun initValues() {
        if (isFirstValidate) {
            topEdge = max(paddingTop, topEdge)
            bottomEdge = max(paddingBottom, bottomEdge)
            getRefreshedRectFRangeBar().let {

                barHeight = (it.bottom - it.top).toInt()
                popMsgThickness = barHeight / 5
                yCenterBar = (it.left + it.right).toInt() / 2
                yTranslationToBottomValueBar = it.bottom
                xTranslationToCenterValueBar = width - paddingRight - barThicknessSize / 2


            }
            isFirstValidate = false
        }


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureHeight(widthMeasureSpec), measureWidth(heightMeasureSpec))


    }

    private fun measureHeight(measureSpec: Int): Int {
        val size: Int = height - paddingTop - paddingBottom
        return resolveSizeAndState(size, measureSpec, 0)

    }

    private fun measureWidth(measureSpec: Int): Int {
        var size: Int = paddingLeft + spaceAfterPopMsg + paddingRight
        val bounds = Rect()
        val labelText = "${maxValue}\n$unitText"
        popMsgPaint.getTextBounds(labelText, 0, labelText.length, bounds)
        size += bounds.width() * 3 / 2
        size += barThicknessSize
        return resolveSizeAndState(size, measureSpec, 0)
    }

    override fun onDraw(canvas: Canvas?) {
        initValues()
        drawBackgroundRect(canvas)
        drawSelectorProgress(canvas)
        super.onDraw(canvas)

    }


    private fun drawSelectorProgress(canvas: Canvas?) {
        val rectF = getRefreshedRectFRangeBar()
        val ratio = (currentValue - minValue).toFloat() / (maxValue - minValue)

        //draw progress line
        val bottom = rectF.bottom * 0.98f
        val top = bottom - (barHeight * ratio * 0.96f)
        val left = yCenterBar - progressLineThickness
        val right = yCenterBar + progressLineThickness
        rectF.set(left.toFloat(), top, right.toFloat(), bottom)

        canvas?.drawRoundRect(rectF, 5f, 5f, selectorPaint)

        //draw selector Thumb
        val thumbTop = top - scrollThumbThickness / 2f
        val thumbBottom = top + scrollThumbThickness / 2f
        val thumbLeft = yCenterBar - barThicknessSize / 2.1
        val thumbRight = yCenterBar + barThicknessSize / 2.1
        rectF.set(
            thumbLeft.toFloat(),
            thumbTop,
            thumbRight.toFloat(),
            thumbBottom
        )
        canvas?.drawRoundRect(rectF, 100f, 100f, selectorPaint)

        //draw pop message
        var popTop = top - popMsgThickness / 2f
        var popBottom = top + popMsgThickness / 2f
        val popRight = left - spaceAfterPopMsg
        val popLeft = popRight - popMsgThickness * 1.1
        if (popTop < topEdge) {
            popTop = topEdge.toFloat()
            popBottom = popTop + popMsgThickness
        } else if (popBottom > height - bottomEdge) {
            popBottom = height - bottomEdge.toFloat()
            popTop = popBottom - popMsgThickness

        }

        rectF.set(popLeft.toFloat(), popTop, popRight.toFloat(), popBottom)
        canvas?.drawRoundRect(rectF, popMsgThickness / 2.6f, popMsgThickness / 2.6f, selectorPaint)

        //draw pop msg label
        val xPopText = (popLeft + popRight) / 2
        val bounds = Rect()
        val popText = "$currentValue\n$unitText"
        popMsgPaint.getTextBounds(popText, 0, popText.length, bounds)
        var yPopText = (popTop + popBottom) / 2
        if (unitText != "") {
            yPopText -= bounds.height() / 2
        } else {
            yPopText += bounds.height() / 2
        }

        drawMultiLineText(popText, xPopText.toFloat(), yPopText, canvas)
    }


    private fun drawBackgroundRect(canvas: Canvas?) {

        val rectF = getRefreshedRectFRangeBar()
        canvas?.drawRoundRect(rectF, barThicknessSize / 3.5f, barThicknessSize / 3.5f, rectPaint)


        val totalFreeSpace = barHeight - (lineNumber * crossLineThickness)
        val freeSpaceNumber = lineNumber + 1
        val freeSpaceLength = totalFreeSpace.toFloat() / freeSpaceNumber.toFloat()

        canvas?.save()

        canvas?.translate(
            xTranslationToCenterValueBar.toFloat(),
            yTranslationToBottomValueBar + crossLineThickness / 2f
        )
        repeat(lineNumber) {
            canvas?.translate(0f, -freeSpaceLength - crossLineThickness)
            rectF.set(
                -barThicknessSize / 4f,
                crossLineThickness / 2f,
                +barThicknessSize / 4f,
                -crossLineThickness / 2f
            )
            canvas?.drawRoundRect(rectF, 3f, 3f, crossLinePaint)

        }

        canvas?.restore()

    }

    private fun getRefreshedRectFRangeBar(): RectF {

        val right = width - paddingRight
        val left = right - barThicknessSize
        val top = topEdge
        val bottom = height - bottomEdge

        rectFValueBar.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        return rectFValueBar
    }


    private fun drawMultiLineText(text: String, x: Float, y: Float, canvas: Canvas?) {
        val lines = text.split("\n")
        var txtSize = -popMsgPaint.ascent() + popMsgPaint.descent()

        if (popMsgPaint.style == Paint.Style.FILL_AND_STROKE || popMsgPaint.style == Paint.Style.STROKE) {
            txtSize += popMsgPaint.strokeWidth
        }

        val lineSpace = txtSize * 0.2f
        repeat(lines.size) {
            canvas?.drawText(lines[it], x, y + (txtSize + lineSpace) * (it), popMsgPaint)
        }
    }

    fun setNewValue(newValue: Int) {
        currentValue = newValue
        this.invalidate()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var touchY: Float = event?.y ?: 0f
        val touchX: Float = event?.x ?: 0f
        val rectF = getRefreshedRectFRangeBar()
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (touchY > rectF.bottom || touchY < rectF.top || touchX < rectF.left || touchX > rectF.right) {
                return false
            }


        } else if (event?.action == MotionEvent.ACTION_MOVE) {
            if (touchY < rectF.top) touchY = rectF.top else if (touchY > rectF.bottom) touchY =
                rectF.bottom
            val newVal =
                (rectF.bottom - touchY) * (maxValue - minValue) / (rectF.bottom - rectF.top) + minValue

            if (newVal.toInt() != currentValue) {
                onProgressChangeListener?.onChange(newVal)
                setNewValue(newVal.toInt())
            }


        }


        return true
    }

    fun setOnProgressChangeListener(listener: OnProgressChangeListener) {
        onProgressChangeListener = listener
    }
}