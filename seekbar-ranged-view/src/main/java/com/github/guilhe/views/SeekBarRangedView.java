package com.github.guilhe.views;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by gdelgado on 24/08/2017.
 */

@SuppressWarnings({"unused", "SameParameterValue"})
public class SeekBarRangedView extends View {

    private enum Thumb {
        MIN, MAX
    }

    private static final int INVALID_POINTER_ID = 255;
    private static final int ACTION_POINTER_UP = 0x6;
    private static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00;
    private static final int ACTION_POINTER_INDEX_SHIFT = 8;
    private static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
    private static final int DEFAULT_BACKGROUND_COLOR = Color.argb(0xFF, 0xC0, 0xC0, 0xC0);
    private static final int DEFAULT_PROGRESS_HEIGHT = 10;
    private static final int DEFAULT_MIN_PROGRESS = 0;
    private static final int DEFAULT_MAX_PROGRESS = 100;
    private static final long DEFAULT_ANIMATE_DURATION = 1000;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mScaledTouchSlop;
    private float mDownMotionX;
    private boolean mIsDragging;
    private OnSeekBarRangedChangeListener mCallback;

    private Paint mPaint;
    private RectF mBackgroundLineRect;
    private RectF mProgressLineRect;
    private Thumb mPressedThumb = null;
    private Bitmap mThumbImage;
    private Bitmap mThumbPressedImage;
    private float mThumbHalfWidth;
    private float mThumbHalfHeight;
    private float mThumbPressedHalfWidth;
    private float mThumbPressedHalfHeight;
    private float mPadding;

    private float mBackgroundLineHeight;
    private float mProgressLineHeight;
    private int mProgressBackgroundColor = DEFAULT_BACKGROUND_COLOR;
    private int mProgressColor = DEFAULT_COLOR;
    private ValueAnimator mMinValueAnimator;
    private ValueAnimator mMaxValueAnimator;
    private float mMinValue;
    private float mMaxValue;
    private float mNormalizedMinValue;
    private float mNormalizedMaxValue = 1f;
    private boolean mRounded;

    public interface OnSeekBarRangedChangeListener {
        void onChanged(SeekBarRangedView view, float minValue, float maxValue);

        void onChanging(SeekBarRangedView view, float minValue, float maxValue);
    }

    //<editor-fold desc="Create & Setup logic">
    public SeekBarRangedView(Context context) {
        this(context, DEFAULT_MIN_PROGRESS, DEFAULT_MAX_PROGRESS);
    }

    public SeekBarRangedView(Context context, float min, float max) {
        super(context);
        init(min, max, DEFAULT_PROGRESS_HEIGHT, DEFAULT_PROGRESS_HEIGHT);
    }

    public SeekBarRangedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAttrs(context, attrs);
    }

    public SeekBarRangedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupAttrs(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SeekBarRangedView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupAttrs(context, attrs);
    }

    private void setupAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SeekBarRangedView, 0, 0);

        float min;
        float currentMin;
        float max;
        float currentMax;
        int progressHeight;
        int bgProgressHeight;
        try {
            min = a.getFloat(R.styleable.SeekBarRangedView_min, DEFAULT_MIN_PROGRESS);
            currentMin = a.getFloat(R.styleable.SeekBarRangedView_currentMin, min);
            max = a.getFloat(R.styleable.SeekBarRangedView_max, DEFAULT_MAX_PROGRESS);
            currentMax = a.getFloat(R.styleable.SeekBarRangedView_currentMax, max);
            progressHeight = a.getDimensionPixelSize(R.styleable.SeekBarRangedView_progressHeight, DEFAULT_PROGRESS_HEIGHT);
            bgProgressHeight = a.getDimensionPixelSize(R.styleable.SeekBarRangedView_backgroundHeight, DEFAULT_PROGRESS_HEIGHT);

            mRounded = a.getBoolean(R.styleable.SeekBarRangedView_rounded, false);
            mProgressColor = a.getColor(R.styleable.SeekBarRangedView_progressColor, DEFAULT_COLOR);
            mProgressBackgroundColor = a.getColor(R.styleable.SeekBarRangedView_backgroundColor, DEFAULT_BACKGROUND_COLOR);

            if (a.hasValue(R.styleable.SeekBarRangedView_thumbsResource)) {
                setThumbsImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbsResource, android.R.drawable.radiobutton_off_background), false);
            } else {
                if (a.hasValue(R.styleable.SeekBarRangedView_thumbNormalResource)) {
                    setThumbNormalImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbNormalResource, android.R.drawable.radiobutton_off_background), false);
                }
                if (a.hasValue(R.styleable.SeekBarRangedView_thumbPressedResource)) {
                    setThumbPressedImageResource(a.getResourceId(R.styleable.SeekBarRangedView_thumbPressedResource, android.R.drawable.radiobutton_on_background), false);
                }
            }
        } finally {
            a.recycle();
        }
        init(min, currentMin, max, currentMax, progressHeight, bgProgressHeight);
    }

    private void init(float min, float max, int progressHeight, int bgProgressHeight) {
        init(min, min, max, max, progressHeight, bgProgressHeight);
    }

    private void init(float min, float currentMin, float max, float currentMax, int progressHeight, int bgProgressHeight) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundLineRect = new RectF();
        mProgressLineRect = new RectF();

        if (mThumbImage == null && mThumbPressedImage == null) {
            setThumbNormalImageResource(android.R.drawable.radiobutton_off_background, false);
            setThumbPressedImageResource(android.R.drawable.radiobutton_on_background, false);
        } else if (mThumbImage == null) {
            setThumbNormalImageResource(android.R.drawable.radiobutton_off_background, false);
        } else if (mThumbPressedImage == null) {
            setThumbPressedImageResource(android.R.drawable.radiobutton_on_background, false);
        }

        measureThumb();
        measureThumbPressed();
        updatePadding();
        setBackgroundHeight(bgProgressHeight, false);
        setProgressHeight(progressHeight, false);

        mMinValue = min;
        mMaxValue = max;
        setSelectedMinValue(currentMin);
        setSelectedMaxValue(currentMax);

        // This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScrollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);

        if (!isInEditMode()) {
            mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        }

    }
    //</editor-fold>

    //<editor-fold desc="Setters & Getters">
    public void setOnSeekBarRangedChangeListener(OnSeekBarRangedChangeListener listener) {
        mCallback = listener;
    }

    public void setMinValue(float value) {
        mMinValue = value;
        setSelectedMinValue(getSelectedMinValue());
    }

    public float getMinValue() {
        return mMinValue;
    }

    public void setMaxValue(float value) {
        mMaxValue = value;
        setSelectedMaxVal(getSelectedMaxValue());
    }

    public float getMaxValue() {
        return mMaxValue;
    }

    public void setSelectedMinValue(float value) {
        setSelectedMinValue(value, false);
    }

    public void setSelectedMinValue(float value, boolean animate) {
        setSelectedMinValue(value, animate, DEFAULT_ANIMATE_DURATION);
    }

    public void setSelectedMinValue(float value, boolean animate, long duration) {
        if (animate) {
            if (mMinValueAnimator != null) {
                mMinValueAnimator.cancel();
            }
            mMinValueAnimator = getAnimator(getSelectedMinValue(), value, duration, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    setSelectedMinVal(((Float) valueAnimator.getAnimatedValue()));
                }
            });
            mMinValueAnimator.start();
        } else {
            setSelectedMinVal(value);
        }
    }

    private void setSelectedMinVal(float value) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (mMaxValue - mMinValue == 0) {
            setNormalizedMinValue(0);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
        onChangedValues();
    }

    public float getSelectedMinValue() {
        return normalizedToValue(mNormalizedMinValue);
    }

    public void setSelectedMaxValue(float value) {
        setSelectedMaxValue(value, false);
    }

    public void setSelectedMaxValue(float value, boolean animate) {
        setSelectedMaxValue(value, animate, DEFAULT_ANIMATE_DURATION);
    }

    public void setSelectedMaxValue(float value, boolean animate, long duration) {
        if (animate) {
            if (mMaxValueAnimator != null) {
                mMaxValueAnimator.cancel();
            }
            mMaxValueAnimator = getAnimator(getSelectedMaxValue(), value, duration, new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    setSelectedMaxVal(((Float) valueAnimator.getAnimatedValue()));
                }
            });
            mMaxValueAnimator.start();
        } else {
            setSelectedMaxVal(value);
        }
    }

    private void setSelectedMaxVal(float value) {
        // in case mMinValue == mMaxValue, avoid division by zero when normalizing.
        if (mMaxValue - mMinValue == 0) {
            setNormalizedMaxValue(1f);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
        onChangedValues();
    }

    public float getSelectedMaxValue() {
        return normalizedToValue(mNormalizedMaxValue);
    }

    private ValueAnimator getAnimator(float current, float next, long duration, ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = new ValueAnimator();
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(duration);
        animator.setObjectValues(current, next);
        animator.setEvaluator(new FloatEvaluator() {
            public Integer evaluate(float fraction, float startValue, float endValue) {
                return Math.round(startValue + (endValue - startValue) * fraction);
            }
        });
        animator.addUpdateListener(updateListener);
        return animator;
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized min value to set.
     */
    private void setNormalizedMinValue(float value) {
        mNormalizedMinValue = Math.max(0, Math.min(1, Math.min(value, mNormalizedMaxValue)));
        invalidate();
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.
     *
     * @param value The new normalized max value to set.
     */
    private void setNormalizedMaxValue(float value) {
        mNormalizedMaxValue = Math.max(0, Math.min(1f, Math.max(value, mNormalizedMinValue)));
        invalidate();
    }

    public void setRounded(boolean rounded) {
        mRounded = rounded;
        invalidate();
    }

    /**
     * Set progress bar background height
     *
     * @param height is given in pixels
     */
    public void setBackgroundHeight(float height) {
        setBackgroundHeight(height, true);
    }

    private void setBackgroundHeight(float height, boolean invalidate) {
        mBackgroundLineHeight = height;
        if (invalidate) {
            requestLayout();
        }
    }

    /**
     * Set progress bar progress height
     *
     * @param height is given in pixels
     */
    public void setProgressHeight(float height) {
        setProgressHeight(height, true);
    }

    private void setProgressHeight(float height, boolean invalidate) {
        mProgressLineHeight = height;
        if (invalidate) {
            requestLayout();
        }
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    public void setBackgroundColor(int red, int green, int blue) {
        setBackgroundColor(0xFF, red, green, blue);
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    public void setBackgroundColor(int alpha, int red, int green, int blue) {
        mProgressBackgroundColor = Color.argb(alpha, red, green, blue);
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setBackgroundColor(Color color) {
        setBackgroundColor(color.toArgb());
    }

    /**
     * You can simulate the use of this method with by calling {@link #setBackgroundColor(int)} with ContextCompat:
     * setBackgroundColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setBackgroundColorResource(@ColorRes int resId) {
        setBackgroundColor(getContext().getColor(resId));
    }

    public void setBackgroundColor(int color) {
        mProgressBackgroundColor = color;
        invalidate();
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    public void setProgressColor(int red, int green, int blue) {
        setProgressColor(0xFF, red, green, blue);
    }

    /**
     * Values in HEX, ex 0xFF
     *
     * @param alpha hex value for alpha
     * @param red   hex value for red
     * @param green hex value for green
     * @param blue  hex value for blue
     */
    public void setProgressColor(int alpha, int red, int green, int blue) {
        mProgressColor = Color.argb(alpha, red, green, blue);
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setProgressColor(Color color) {
        setProgressColor(color.toArgb());
    }

    /**
     * You can simulate the use of this method with by calling {@link #setProgressColor(int)} with ContextCompat:
     * setProgressColor(ContextCompat.getColor(resId));
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setProgressColorResource(@ColorRes int resId) {
        setProgressColor(getContext().getColor(resId));
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
        invalidate();
    }

    public void setThumbsImage(Bitmap bitmap) {
        setThumbsImage(bitmap, true);
        setThumbPressedImage(bitmap, true);
    }

    private void setThumbsImage(Bitmap bitmap, boolean requestLayout) {
        setThumbNormalImage(bitmap, requestLayout);
        setThumbPressedImage(bitmap, requestLayout);
    }

    public void setThumbsImageResource(@DrawableRes int resId) {
        setThumbNormalImageResource(resId);
        setThumbPressedImageResource(resId);
    }

    private void setThumbsImageResource(@DrawableRes int resId, boolean requestLayout) {
        setThumbNormalImageResource(resId, requestLayout);
        setThumbPressedImageResource(resId, requestLayout);
    }

    public void setThumbNormalImage(Bitmap bitmap) {
        setThumbNormalImage(bitmap, true);
    }

    private void setThumbNormalImage(Bitmap bitmap, boolean requestLayout) {
        mThumbImage = bitmap;
        mThumbPressedImage = mThumbPressedImage == null ? mThumbImage : mThumbPressedImage;
        measureThumb();
        updatePadding();
        if (requestLayout) {
            requestLayout();
        }
    }

    public void setThumbNormalImageResource(@DrawableRes int resId) {
        setThumbNormalImageResource(resId, true);
    }

    private void setThumbNormalImageResource(@DrawableRes int resId, boolean requestLayout) {
        mThumbImage = BitmapFactory.decodeResource(getResources(), resId);
        mThumbPressedImage = mThumbPressedImage == null ? mThumbImage : mThumbPressedImage;
        measureThumb();
        updatePadding();
        if (requestLayout) {
            requestLayout();
        }
    }

    public void setThumbPressedImage(Bitmap bitmap) {
        setThumbPressedImage(bitmap, true);
    }

    private void setThumbPressedImage(Bitmap bitmap, boolean requestLayout) {
        mThumbPressedImage = bitmap;
        mThumbImage = mThumbImage == null ? mThumbPressedImage : mThumbImage;
        measureThumbPressed();
        updatePadding();
        if (requestLayout) {
            requestLayout();
        }
    }

    public void setThumbPressedImageResource(@DrawableRes int resId) {
        setThumbPressedImageResource(resId, true);
    }

    private void setThumbPressedImageResource(@DrawableRes int resId, boolean requestLayout) {
        mThumbPressedImage = BitmapFactory.decodeResource(getResources(), resId);
        mThumbImage = mThumbImage == null ? mThumbPressedImage : mThumbImage;
        measureThumbPressed();
        updatePadding();
        if (requestLayout) {
            requestLayout();
        }
    }
    //</editor-fold>

    private void onChangedValues() {
        if (mCallback != null) {
            mCallback.onChanged(this, getSelectedMinValue(), getSelectedMaxValue());
        }
    }

    private void onChangingValues() {
        if (mCallback != null) {
            mCallback.onChanging(this, getSelectedMinValue(), getSelectedMaxValue());
        }
    }

    private void measureThumb() {
        mThumbHalfWidth = 0.5f * mThumbImage.getWidth();
        mThumbHalfHeight = 0.5f * mThumbImage.getHeight();
    }

    private void measureThumbPressed() {
        mThumbPressedHalfWidth = 0.5f * mThumbPressedImage.getWidth();
        mThumbPressedHalfHeight = 0.5f * mThumbPressedImage.getHeight();
    }

    private void updatePadding() {
        mPadding = Math.max(Math.max(mThumbHalfWidth, mThumbPressedHalfWidth), Math.max(mThumbHalfHeight, mThumbPressedHalfHeight));
    }

    //<editor-fold desc="Value converters">

    /**
     * Converts a normalized value to a value space between absolute minimum and maximum.
     *
     * @param normalized The value to "de-normalize".
     * @return The "de-normalized" value.
     */
    private float normalizedToValue(float normalized) {
        return mMinValue + normalized * (mMaxValue - mMinValue);
    }

    /**
     * Converts the given value to a normalized value.
     *
     * @param value The value to normalize.
     * @return The normalized value.
     */
    private float valueToNormalized(float value) {
        if (0 == mMaxValue - mMinValue) {
            // prevent division by zero, simply return 0.
            return 0;
        }
        return (value - mMinValue) / (mMaxValue - mMinValue);
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoordinate The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreen(float normalizedCoordinate) {
        return mPadding + normalizedCoordinate * (getWidth() - 2 * mPadding);
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoordinate The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private float screenToNormalized(float screenCoordinate) {
        int width = getWidth();
        if (width <= 2 * mPadding) {
            // prevent division by zero, simply return 0.
            return 0;
        } else {
            float result = (screenCoordinate - mPadding) / (width - 2 * mPadding);
            return Math.min(1, Math.max(0, result));
        }
    }
    //</editor-fold>

    //<editor-fold desc="Touch logic">
    private void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);

        if (Thumb.MIN.equals(mPressedThumb)) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(mPressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void onStartTrackingTouch() {
        mIsDragging = true;
    }

    private void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRange(touchX, mNormalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, mNormalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a
            // corner, not being able to drag them apart anymore.
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.
     *
     * @param touchX               The x-coordinate in screen space to check.
     * @param normalizedThumbValue The normalized x-coordinate of the thumb to check.
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private boolean isInThumbRange(float touchX, float normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= mThumbHalfWidth;
    }
    //</editor-fold>

    //<editor-fold desc="View life-cycle">
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int maxThumb = Math.max(mThumbImage.getHeight(), mThumbPressedImage.getHeight());
        int maxHeight = (int) Math.max(mProgressLineHeight, mBackgroundLineHeight);
        int height = Math.max(maxThumb, maxHeight);
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setStyle(Style.FILL);
        mPaint.setAntiAlias(true);

        // draw seek bar background line
        float corners = Math.max(mBackgroundLineHeight, mProgressLineHeight) * (mRounded ? 0.5f : 0);
        mBackgroundLineRect.set(mPadding, 0.5f * (getHeight() - mBackgroundLineHeight), getWidth() - mPadding, 0.5f * (getHeight() + mBackgroundLineHeight));
        mPaint.setColor(mProgressBackgroundColor);
        canvas.drawRoundRect(mBackgroundLineRect, corners, corners, mPaint);

        mBackgroundLineRect.left = normalizedToScreen(mNormalizedMinValue);
        mBackgroundLineRect.right = normalizedToScreen(mNormalizedMaxValue);

        // draw seek bar progress line
        mProgressLineRect.set(mPadding, 0.5f * (getHeight() - mProgressLineHeight), getWidth() - mPadding, 0.5f * (getHeight() + mProgressLineHeight));
        mProgressLineRect.left = normalizedToScreen(mNormalizedMinValue);
        mProgressLineRect.right = normalizedToScreen(mNormalizedMaxValue);

        mPaint.setColor(mProgressColor);
        canvas.drawRoundRect(mProgressLineRect, corners, corners, mPaint);

        // draw minimum thumb
        drawThumb(canvas, normalizedToScreen(mNormalizedMinValue), Thumb.MIN.equals(mPressedThumb));

        // draw maximum thumb
        drawThumb(canvas, normalizedToScreen(mNormalizedMaxValue), Thumb.MAX.equals(mPressedThumb));
    }

    /**
     * @param canvas           The canvas to draw upon.
     * @param screenCoordinate The x-coordinate in screen space where to draw the image.
     * @param pressed          Is the thumb currently in "pressed" state?
     */
    private void drawThumb(Canvas canvas, float screenCoordinate, boolean pressed) {
        canvas.drawBitmap(
                pressed ? mThumbPressedImage : mThumbImage
                , screenCoordinate - (pressed ? mThumbPressedHalfWidth : mThumbHalfWidth)
                , (0.5f * getHeight()) - (pressed ? mThumbPressedHalfHeight : mThumbHalfHeight)
                , mPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                mPressedThumb = evalPressedThumb(mDownMotionX);

                // Only handle thumb presses.
                if (mPressedThumb == null) {
                    return super.onTouchEvent(event);
                }

                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPressedThumb != null) {
                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    onChangingValues();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                mPressedThumb = null;
                invalidate();
                if (mCallback != null) {
                    mCallback.onChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", mNormalizedMinValue);
        bundle.putDouble("MAX", mNormalizedMaxValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        mNormalizedMinValue = bundle.getFloat("MIN");
        mNormalizedMaxValue = bundle.getFloat("MAX");
        onChangedValues();
        onChangingValues();
    }
    //</editor-fold>
}