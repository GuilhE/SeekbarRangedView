package com.github.guilhe.views

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import com.github.guilhe.views.rangedseekbar.R
import java.util.*
import kotlin.math.*

@Suppress("MemberVisibilityCanBePrivate", "unused")
class SeekBarRangedView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var thumbImage: Bitmap
    private lateinit var thumbPressedImage: Bitmap
    private lateinit var minValueAnimator: ValueAnimator
    private lateinit var maxValueAnimator: ValueAnimator

    private var activePointerId = INVALID_POINTER_ID
    private var scaledTouchSlop = 0
    private var downMotionX = 0f
    private var isDragging = false

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundLineRect = RectF()
    private var progressLineRect = RectF()
    private var pressedThumb: Thumb? = null
    private var thumbHalfWidth = 0f
    private var thumbHalfHeight = 0f
    private var thumbPressedHalfWidth = 0f
    private var thumbPressedHalfHeight = 0f
    private var padding = 0f
    private var backgroundLineHeight = 0f
    private var progressLineHeight = 0f
    private var progressBackgroundColor = DEFAULT_BACKGROUND_COLOR
    private var progressColor = DEFAULT_COLOR
    private var isRounded = false
    private var isStepProgressEnable = false
    private val progressStepList: MutableList<Float> = ArrayList()
    private var stepRadius = DEFAULT_STEP_RADIUS.toFloat()

    private var normalizedMinValue = 0f
    private var normalizedMaxValue = 1f

    var actionCallback: SeekBarRangedChangeCallback? = null

    var minValue = 0f
        private set

    var maxValue = 0f
        private set

    var selectedMinValue: Float
        get() = normalizedToValue(normalizedMinValue)
        set(value) {
            setSelectedMinValue(value, false)
        }

    var selectedMaxValue: Float
        get() = normalizedToValue(normalizedMaxValue)
        set(value) {
            setSelectedMaxValue(value, false)
        }

    interface SeekBarRangedChangeCallback {
        fun onChanged(minValue: Float, maxValue: Float)
        fun onChanging(minValue: Float, maxValue: Float)
    }

    init {
        setupAttrs(context, attrs)
    }

    private fun setupAttrs(context: Context, attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SeekBarRangedView, 0, 0)
        val min: Float
        val currentMin: Float
        val max: Float
        val currentMax: Float
        val progressHeight: Int
        val bgProgressHeight: Int
        try {
            min = a.getFloat(R.styleable.SeekBarRangedView_min, DEFAULT_MIN_PROGRESS)
            currentMin = a.getFloat(R.styleable.SeekBarRangedView_currentMin, min)
            max = a.getFloat(R.styleable.SeekBarRangedView_max, DEFAULT_MAX_PROGRESS)
            currentMax = a.getFloat(R.styleable.SeekBarRangedView_currentMax, max)
            progressHeight = a.getDimensionPixelSize(R.styleable.SeekBarRangedView_progressHeight, DEFAULT_PROGRESS_HEIGHT)
            bgProgressHeight = a.getDimensionPixelSize(R.styleable.SeekBarRangedView_backgroundHeight, DEFAULT_PROGRESS_HEIGHT)
            isRounded = a.getBoolean(R.styleable.SeekBarRangedView_rounded, false)
            progressColor = a.getColor(R.styleable.SeekBarRangedView_progressColor, DEFAULT_COLOR)
            progressBackgroundColor = a.getColor(R.styleable.SeekBarRangedView_backgroundColor, DEFAULT_BACKGROUND_COLOR)
            if (a.hasValue(R.styleable.SeekBarRangedView_thumbsResource)) {
                setThumbsImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbsResource, R.drawable.default_thumb))
            } else {
                if (a.hasValue(R.styleable.SeekBarRangedView_thumbNormalResource)) {
                    setThumbNormalImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbNormalResource, R.drawable.default_thumb))
                }
                if (a.hasValue(R.styleable.SeekBarRangedView_thumbPressedResource)) {
                    setThumbPressedImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbPressedResource, R.drawable.default_thumb_pressed))
                }
            }
        } finally {
            a.recycle()
        }
        init(min, currentMin, max, currentMax, progressHeight, bgProgressHeight)
    }

    private fun init(min: Float, currentMin: Float, max: Float, currentMax: Float, progressHeight: Int, bgProgressHeight: Int) {
        if (::thumbImage.isInitialized.not() && ::thumbPressedImage.isInitialized.not()) {
            setThumbNormalImageResource(R.drawable.default_thumb)
            setThumbPressedImageResource(R.drawable.default_thumb_pressed)
        } else if (::thumbImage.isInitialized.not()) {
            setThumbNormalImageResource(R.drawable.default_thumb)
        } else if (::thumbPressedImage.isInitialized.not()) {
            setThumbPressedImageResource(R.drawable.default_thumb_pressed)
        }
        measureThumb()
        measureThumbPressed()
        updatePadding()
        setBackgroundHeight(bgProgressHeight.toFloat(), false)
        setProgressHeight(progressHeight.toFloat(), false)
        minValue = min
        maxValue = max
        selectedMinValue = currentMin
        selectedMaxValue = currentMax

        // This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScrollViews.
        isFocusable = true
        isFocusableInTouchMode = true
        if (!isInEditMode) {
            scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        }
    }

    /**
     * This method will change the min value to a desired value. Note that if Progress by Steps is enabled, min will stay as default.
     *
     * @param value new min value
     * @return true if changed
     */
    fun setMinValue(value: Float): Boolean {
        if (isStepProgressEnable) {
            return false
        }
        minValue = value
        selectedMinValue = selectedMinValue
        return true
    }

    /**
     * This method will change the max value to a desired value. Note that if Progress by Steps is enabled, max will stay as default.
     *
     * @param value new max value
     * @return true if changed
     */
    fun setMaxValue(value: Float): Boolean {
        if (isStepProgressEnable) {
            return false
        }
        maxValue = value
        setSelectedMaxVal(selectedMaxValue)
        return true
    }

    fun setSelectedMinValue(value: Float, animate: Boolean) {
        setSelectedMinValue(value, animate, DEFAULT_ANIMATE_DURATION)
    }

    fun setSelectedMinValue(value: Float, animate: Boolean, duration: Long) {
        if (animate) {
            if (::minValueAnimator.isInitialized) {
                minValueAnimator.cancel()
            }
            minValueAnimator = getAnimator(selectedMinValue, value, duration) { valueAnimator -> setSelectedMinVal(valueAnimator.animatedValue as Float) }
            minValueAnimator.start()
        } else {
            setSelectedMinVal(value)
        }
    }

    private fun setSelectedMinVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maxValue - minValue == 0f) {
            setNormalizedMinValue(0f)
        } else {
            setNormalizedMinValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    fun setSelectedMaxValue(value: Float, animate: Boolean) {
        setSelectedMaxValue(value, animate, DEFAULT_ANIMATE_DURATION)
    }

    fun setSelectedMaxValue(value: Float, animate: Boolean, duration: Long) {
        if (animate) {
            if (::maxValueAnimator.isInitialized) {
                maxValueAnimator.cancel()
            }
            maxValueAnimator = getAnimator(selectedMaxValue, value, duration) { valueAnimator -> setSelectedMaxVal(valueAnimator.animatedValue as Float) }
            maxValueAnimator.start()
        } else {
            setSelectedMaxVal(value)
        }
    }

    private fun setSelectedMaxVal(value: Float) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (maxValue - minValue == 0f) {
            setNormalizedMaxValue(1f)
        } else {
            setNormalizedMaxValue(valueToNormalized(value))
        }
        onChangedValues()
    }

    private fun getAnimator(current: Float, next: Float, duration: Long, updateListener: AnimatorUpdateListener) = ValueAnimator().apply {
        this.interpolator = DecelerateInterpolator()
        this.duration = duration
        this.setObjectValues(current, next)
        this.setEvaluator(object : FloatEvaluator() {
            fun evaluate(fraction: Float, startValue: Float, endValue: Float): Int {
                return (startValue + (endValue - startValue) * fraction).roundToInt()
            }
        })
        this.addUpdateListener(updateListener)
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private fun setNormalizedMinValue(value: Float) {
        normalizedMinValue = max(0f, min(1f, min(value, normalizedMaxValue)))
        invalidate()
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private fun setNormalizedMaxValue(value: Float) {
        normalizedMaxValue = max(0f, min(1f, max(value, normalizedMinValue)))
        invalidate()
    }

    fun setRounded(rounded: Boolean) {
        isRounded = rounded
        invalidate()
    }

    /**
     * Set progress bar background height
     *
     * @param height is given in pixels
     */
    fun setBackgroundHeight(height: Float) {
        setBackgroundHeight(height, true)
    }

    private fun setBackgroundHeight(height: Float, invalidate: Boolean) {
        backgroundLineHeight = height
        if (invalidate) {
            requestLayout()
        }
    }

    /**
     * Set progress bar progress height
     *
     * @param height is given in pixels
     */
    fun setProgressHeight(height: Float) {
        setProgressHeight(height, true)
    }

    private fun setProgressHeight(height: Float, invalidate: Boolean) {
        progressLineHeight = height
        if (invalidate) {
            requestLayout()
        }
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setBackgroundColor(red: Int, green: Int, blue: Int) {
        setBackgroundColor(0xFF, red, green, blue)
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setBackgroundColor(alpha: Int, red: Int, green: Int, blue: Int) {
        progressBackgroundColor = Color.argb(alpha, red, green, blue)
        invalidate()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setBackgroundColor(color: Color) {
        setBackgroundColor(color.toArgb())
    }

    /**
     * You can simulate the use of this method with by calling [.setBackgroundColor] with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setBackgroundColorResource(@ColorRes resId: Int) {
        setBackgroundColor(context.getColor(resId))
    }

    override fun setBackgroundColor(color: Int) {
        progressBackgroundColor = color
        invalidate()
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setProgressColor(red: Int, green: Int, blue: Int) {
        setProgressColor(0xFF, red, green, blue)
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    fun setProgressColor(alpha: Int, red: Int, green: Int, blue: Int) {
        progressColor = Color.argb(alpha, red, green, blue)
        invalidate()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun setProgressColor(color: Color) {
        setProgressColor(color.toArgb())
    }

    /**
     * You can simulate the use of this method with by calling [.setProgressColor] with ContextCompat:
     * setProgressColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun setProgressColorResource(@ColorRes resId: Int) {
        setProgressColor(context.getColor(resId))
    }

    fun setProgressColor(color: Int) {
        progressColor = color
        invalidate()
    }

    fun setThumbsImage(bitmap: Bitmap) {
        setThumbNormalImage(bitmap)
        setThumbPressedImage(bitmap)
    }

    fun setThumbsImageResource(@DrawableRes resId: Int) {
        setThumbNormalImageResource(resId)
        setThumbPressedImageResource(resId)
    }

    fun setThumbNormalImage(bitmap: Bitmap) {
        thumbImage = bitmap
        thumbPressedImage = if (::thumbPressedImage.isInitialized.not()) thumbImage else thumbPressedImage
        measureThumb()
        updatePadding()
        requestLayout()
    }

    fun setThumbNormalImageResource(@DrawableRes resId: Int) {
        val d = resources.getDrawable(resId, null)
        thumbImage = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        d.draw(Canvas(thumbImage))
        setThumbNormalImage(thumbImage)
    }

    fun setThumbPressedImage(bitmap: Bitmap) {
        thumbPressedImage = bitmap
        thumbImage = if (::thumbImage.isInitialized.not()) thumbPressedImage else thumbImage
        measureThumbPressed()
        updatePadding()
        requestLayout()
    }

    fun setThumbPressedImageResource(@DrawableRes resId: Int) {
        val d = resources.getDrawable(resId, null)
        thumbPressedImage = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
        d.draw(Canvas(thumbPressedImage))
        setThumbPressedImage(thumbPressedImage)
    }

    //</editor-fold>
    private fun onChangedValues() {
        actionCallback?.onChanged(selectedMinValue, selectedMaxValue)
    }

    private fun onChangingValues() {
        actionCallback?.onChanging(selectedMinValue, selectedMaxValue)
    }

    private fun measureThumb() {
        thumbHalfWidth = 0.5f * thumbImage.width
        thumbHalfHeight = 0.5f * thumbImage.height
    }

    private fun measureThumbPressed() {
        thumbPressedHalfWidth = 0.5f * thumbPressedImage.width
        thumbPressedHalfHeight = 0.5f * thumbPressedImage.height
    }

    private fun updatePadding() {
        val thumbWidth = max(thumbHalfWidth, thumbPressedHalfWidth)
        val thumbHeight = max(thumbHalfHeight, thumbPressedHalfHeight)
        padding = max(max(thumbWidth, thumbHeight), stepRadius)
    }

    /**
     * Converts a normalized value to a value space between absolute minimum and maximum.
     *
     * @param normalized The value to "de-normalize".
     * @return The "de-normalized" value.
     */
    private fun normalizedToValue(normalized: Float): Float {
        return minValue + normalized * (maxValue - minValue)
    }

    /**
     * Converts the given value to a normalized value.
     *
     * @param value The value to normalize.
     * @return The normalized value.
     */
    private fun valueToNormalized(value: Float): Float {
        return if (0f == maxValue - minValue) 0f /* prevent division by zero */ else (value - minValue) / (maxValue - minValue)
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoordinate The normalized value to convert.
     * @return The converted value in screen space.
     */
    private fun normalizedToScreen(normalizedCoordinate: Float): Float {
        return padding + normalizedCoordinate * (width - 2 * padding)
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoordinate The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private fun screenToNormalized(screenCoordinate: Float): Float {
        val width = width
        return if (width <= 2 * padding) {
            0f //prevent division by zero
        } else {
            val result = (screenCoordinate - padding) / (width - 2 * padding)
            min(1f, max(0f, result))
        }
    }

    //<editor-fold desc="Touch logic">
    private fun trackTouchEvent(event: MotionEvent) {
        val pointerIndex = event.findPointerIndex(activePointerId)
        var x = event.getX(pointerIndex)
        if (isStepProgressEnable) {
            x = getClosestStep(screenToNormalized(x))
        }
        if (Thumb.MIN == pressedThumb) {
            setNormalizedMinValue(if (isStepProgressEnable) x else screenToNormalized(x))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(if (isStepProgressEnable) x else screenToNormalized(x))
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.action and ACTION_POINTER_INDEX_MASK shr ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            downMotionX = ev.getX(newPointerIndex)
            activePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag() {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    private fun onStartTrackingTouch() {
        isDragging = true
    }

    private fun onStopTrackingTouch() {
        isDragging = false
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private fun evalPressedThumb(touchX: Float): Thumb? {
        var result: Thumb? = null
        val minThumbPressed = isInThumbRange(touchX, normalizedMinValue)
        val maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue)
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a
            // corner, not being able to drag them apart anymore.
            result = if (touchX / width > 0.5f) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private fun isInThumbRange(touchX: Float, normalizedThumbValue: Float): Boolean {
        return abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth
    }
    //</editor-fold>

    //<editor-fold desc="Progress-by-Step logic">
    /**
     * When enabled, min and max are set to 0 and 100 (default values) and cannot be changed
     *
     * @param enable if true, enables Progress by Step
     */
    fun enableProgressBySteps(enable: Boolean) {
        isStepProgressEnable = enable
        if (enable) {
            setMinValue(DEFAULT_MIN_PROGRESS)
            setMaxValue(DEFAULT_MAX_PROGRESS)
        }
        invalidate()
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    fun setProgressSteps(vararg steps: Float) {
        val res: MutableList<Float> = ArrayList()
        for (step in steps) {
            res.add(step)
        }
        setProgressSteps(res)
    }

    /**
     * Note: 0 and 100 will automatically be added as min and max respectively, you don't need to add it again.
     *
     * @param steps values for each step
     */
    fun setProgressSteps(steps: List<Float>) {
        progressStepList.clear()
        progressStepList.add(valueToNormalized(DEFAULT_MIN_PROGRESS))
        for (step in steps) {
            progressStepList.add(valueToNormalized(step))
        }
        progressStepList.add(valueToNormalized(DEFAULT_MAX_PROGRESS))
        invalidate()
    }

    /**
     * @param radius in pixels
     */
    fun setProgressStepRadius(radius: Float) {
        stepRadius = radius
        updatePadding()
        invalidate()
    }

    val progressSteps: List<Float>
        get() {
            val res: MutableList<Float> = ArrayList()
            for (step in progressStepList) {
                res.add(normalizedToValue(step))
            }
            return res
        }

    private fun getClosestStep(value: Float): Float {
        var min = abs(progressStepList[0] - value)
        var currentMin: Float
        var colesest = 0f
        for (step in progressStepList) {
            currentMin = abs(step - value)
            if (currentMin < min) {
                colesest = step
                min = currentMin
            }
        }
        return colesest
    }

    //</editor-fold>

    //<editor-fold desc="View life-cycle">
    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = MeasureSpec.getSize(widthMeasureSpec)
        }
        val maxThumb = max(thumbImage.height, thumbPressedImage.height)
        val maxHeight = max(progressLineHeight, backgroundLineHeight).toInt()
        var height = max(max(maxThumb, maxHeight), dpToPx(stepRadius))
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            height = min(height, MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        // draw seek bar background line
        val corners: Float = max(backgroundLineHeight, progressLineHeight) * if (isRounded) 0.5f else 0f
        backgroundLineRect[padding, 0.5f * (height - backgroundLineHeight), width - padding] = 0.5f * (height + backgroundLineHeight)
        paint.color = progressBackgroundColor
        canvas.drawRoundRect(backgroundLineRect, corners, corners, paint)
        backgroundLineRect.left = normalizedToScreen(normalizedMinValue)
        backgroundLineRect.right = normalizedToScreen(normalizedMaxValue)

        // draw seek bar progress line
        progressLineRect[padding, 0.5f * (height - progressLineHeight), width - padding] = 0.5f * (height + progressLineHeight)
        progressLineRect.left = normalizedToScreen(normalizedMinValue)
        progressLineRect.right = normalizedToScreen(normalizedMaxValue)
        paint.color = progressColor
        canvas.drawRoundRect(progressLineRect, corners, corners, paint)
        val minX = normalizedToScreen(normalizedMinValue)
        val maxX = normalizedToScreen(normalizedMaxValue)

        // draw progress steps, if enabled
        if (isStepProgressEnable) {
            var stepX: Float
            for (step in progressStepList) {
                stepX = normalizedToScreen(step)
                paint.color = if (stepX > maxX || stepX < minX) progressBackgroundColor else progressColor
                drawStep(canvas, normalizedToScreen(step), stepRadius, paint)
            }
        }

        // draw thumbs
        drawThumb(canvas, minX, Thumb.MIN == pressedThumb)
        drawThumb(canvas, maxX, Thumb.MAX == pressedThumb)
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the image.
     * @param pressed          Is the thumb currently in "pressed" state
     */
    private fun drawThumb(canvas: Canvas, screenCoordinate: Float, pressed: Boolean) {
        canvas.drawBitmap(
                if (pressed) thumbPressedImage else thumbImage,
                screenCoordinate - if (pressed) thumbPressedHalfWidth else thumbHalfWidth,
                0.5f * height - if (pressed) thumbPressedHalfHeight else thumbHalfHeight, paint)
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the step.
     * @param radius           Step circle radius
     * @param paint            Paint to color the steps
     */
    private fun drawStep(canvas: Canvas, screenCoordinate: Float, radius: Float, paint: Paint) {
        canvas.drawCircle(screenCoordinate, 0.5f * height, radius, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val pointerIndex: Int
        val action = event.action
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                activePointerId = event.getPointerId(event.pointerCount - 1)
                pointerIndex = event.findPointerIndex(activePointerId)
                downMotionX = event.getX(pointerIndex)
                pressedThumb = evalPressedThumb(downMotionX)

                // Only handle thumb presses.
                if (pressedThumb == null) {
                    return super.onTouchEvent(event)
                }
                isPressed = true
                invalidate()
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {
                if (isDragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.getX(pointerIndex)
                    if (abs(x - downMotionX) > scaledTouchSlop) {
                        isPressed = true
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }
                onChangingValues()
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                pressedThumb = null
                invalidate()
                actionCallback?.onChanged(selectedMinValue, selectedMaxValue)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.pointerCount - 1
                // final int index = ev.getActionIndex();
                downMotionX = event.getX(index)
                activePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
            else -> {
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putFloat("MIN", normalizedMinValue)
        bundle.putFloat("MAX", normalizedMaxValue)
        bundle.putFloat("MIN_RANGE", minValue)
        bundle.putFloat("MAX_RANGE", maxValue)
        return bundle
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"))
        normalizedMinValue = bundle.getFloat("MIN")
        normalizedMaxValue = bundle.getFloat("MAX")
        minValue = bundle.getFloat("MIN_RANGE")
        maxValue = bundle.getFloat("MAX_RANGE")
        onChangedValues()
        onChangingValues()
    }

    //</editor-fold>

    private fun dpToPx(dp: Float) = ceil(dp * Resources.getSystem().displayMetrics.density.toDouble()).toInt()

    private enum class Thumb {
        MIN, MAX
    }

    companion object {
        private const val INVALID_POINTER_ID = 255
        private const val ACTION_POINTER_UP = 0x6
        private const val ACTION_POINTER_INDEX_MASK = 0x0000ff00
        private const val ACTION_POINTER_INDEX_SHIFT = 8
        private val DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5)
        private val DEFAULT_BACKGROUND_COLOR = Color.argb(0xFF, 0xC0, 0xC0, 0xC0)
        private const val DEFAULT_PROGRESS_HEIGHT = 10
        private const val DEFAULT_STEP_RADIUS = DEFAULT_PROGRESS_HEIGHT + 2
        private const val DEFAULT_MIN_PROGRESS = 0f
        private const val DEFAULT_MAX_PROGRESS = 100f
        private const val DEFAULT_ANIMATE_DURATION: Long = 1000
    }
}

@Suppress("unused")
inline fun SeekBarRangedView.addActionListener(
        crossinline onChanged: (minValue: Float, maxValue: Float) -> Unit = { _, _ -> },
        crossinline onChanging: (minValue: Float, maxValue: Float) -> Unit = { _, _ -> },
): SeekBarRangedView.SeekBarRangedChangeCallback {
    val callback = object : SeekBarRangedView.SeekBarRangedChangeCallback {

        override fun onChanged(minValue: Float, maxValue: Float) {
            onChanged.invoke(minValue, maxValue)
        }

        override fun onChanging(minValue: Float, maxValue: Float) {
            onChanging.invoke(minValue, maxValue)
        }
    }
    actionCallback = callback
    return callback
}