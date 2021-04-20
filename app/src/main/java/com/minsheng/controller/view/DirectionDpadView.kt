package com.minsheng.controller.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.minsheng.controller.R
import com.minsheng.controller.util.Utils
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * ClassName:      DirectionDpadView
 * Author:         LiMinsheng
 * Date:           2021/4/20 10:28
 * Description:    四个方向键+center键
 */
class DirectionDpadView : View, GestureDetector.OnGestureListener {

    companion object {
        const val TAG = "DirectionDpadView"
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private var mBitmapDpadBg =
        Utils.getBitmap(R.mipmap.dpad_background, Utils.dp2px(240f).toInt(), resources)

    private var mBitmapDpadMask =
        Utils.getBitmap(R.mipmap.dpad_5way_normal, Utils.dp2px(240f).toInt(), resources)
    private var mBitmapDpadCenter =
        Utils.getBitmap(R.mipmap.dpad_center_normal, Utils.dp2px(80f).toInt(), resources)

    private val mGestureDetectorCompat by lazy {
        GestureDetectorCompat(context, this)
    }
    private var mOkRadius = 0f
    private var mPressKey = -1
    private var mOnKeyListener: OnDirectionKeyListener? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmapDpadBg = Utils.getBitmap(R.mipmap.dpad_background, width, resources)
        mBitmapDpadMask = Utils.getBitmap(R.mipmap.dpad_5way_normal, width, resources)
        mOkRadius = 2 * width / 5f / 2f
        mBitmapDpadCenter =
            Utils.getBitmap(R.mipmap.dpad_center_normal, (mOkRadius * 2).toInt(), resources)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawBitmap(mBitmapDpadBg, 0f, 0f, mPaint)
        canvas.drawBitmap(
            mBitmapDpadCenter,
            (width - mBitmapDpadCenter.width) / 2f,
            (height - mBitmapDpadCenter.height) / 2f,
            mPaint
        )
        canvas.drawBitmap(mBitmapDpadMask, 0f, 0f, mPaint)
    }

    private fun handlePress() {
        val bitmapBgId = when (mPressKey) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                R.mipmap.dpad_background
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                R.mipmap.dpad_up_pressed
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                R.mipmap.dpad_down_pressed
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                R.mipmap.dpad_left_pressed
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                R.mipmap.dpad_right_pressed
            }
            else -> {
                R.mipmap.dpad_background
            }
        }
        val bitmapCenterId = when (mPressKey) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                R.mipmap.dpad_center_pressed
            }
            else -> {
                R.mipmap.dpad_center_normal
            }
        }
        mBitmapDpadBg = Utils.getBitmap(bitmapBgId, width, resources)
        mBitmapDpadCenter = Utils.getBitmap(bitmapCenterId, (mOkRadius * 2).toInt(), resources)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetectorCompat.onTouchEvent(event)
    }

    override fun onShowPress(p0: MotionEvent?) {

    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        if (mPressKey != -1) mOnKeyListener?.onClick(mPressKey)
        when (mPressKey) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                Log.d(TAG, "onSingleTapUp: 点击了Ok")
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                Log.d(TAG, "onSingleTapUp: 点击了Up")
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                Log.d(TAG, "onSingleTapUp: 点击了Down")
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                Log.d(TAG, "onSingleTapUp: 点击了Left")
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                Log.d(TAG, "onSingleTapUp: 点击了Right")
            }
            else -> {
                Log.d(TAG, "onDown: 无效点击")
            }
        }
        mPressKey = -1
        handlePress()
        return true
    }

    override fun onDown(event: MotionEvent): Boolean {
        mPressKey = -1
        //转换成已键盘中心为坐标原点的坐标
        val x = event.x - width / 2f
        val y = event.y - height / 2f
        //点击位置距离键盘中心原点距离必须小于半径
        val tapRadius = sqrt(x * x + y * y)
        if (tapRadius <= width / 2f) {
            if (abs(x) <= mOkRadius && abs(y) <= mOkRadius) {
                mPressKey = KeyEvent.KEYCODE_DPAD_CENTER
            } else if (abs(y) > mOkRadius && y < 0 && abs(y) > abs(x)) {
                mPressKey = KeyEvent.KEYCODE_DPAD_UP
            } else if (abs(y) > mOkRadius && y > 0 && abs(y) > abs(x)) {
                mPressKey = KeyEvent.KEYCODE_DPAD_DOWN
            } else if (x < 0 && abs(x) > mOkRadius && abs(x) > abs(y)) {
                mPressKey = KeyEvent.KEYCODE_DPAD_LEFT
            } else if (x > 0 && abs(x) > mOkRadius && abs(x) > abs(y)) {
                mPressKey = KeyEvent.KEYCODE_DPAD_RIGHT
            }
        }
        handlePress()
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent?) {
        if (mPressKey != -1) mOnKeyListener?.onLongPress(mPressKey)
        when (mPressKey) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                Log.d(TAG, "onLongPress: 长按了Ok")
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                Log.d(TAG, "onLongPress: 长按了Up")
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                Log.d(TAG, "onLongPress: 长按了Down")
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                Log.d(TAG, "onLongPress: 长按了Left")
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                Log.d(TAG, "onLongPress: 长按了Right")
            }
            else -> {
                Log.d(TAG, "onLongPress: 长按点击")
            }
        }
        mPressKey = -1
        handlePress()
    }

    interface OnDirectionKeyListener {
        fun onClick(keyCode: Int)
        fun onLongPress(keyCode: Int)
    }

    fun setOnDirectionKeyListener(listener: OnDirectionKeyListener) {
        this.mOnKeyListener = listener
    }

}