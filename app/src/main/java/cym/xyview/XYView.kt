package cym.xyview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import androidx.core.content.ContextCompat
import kotlin.math.abs

/**
 * 十字坐标系自定义View
 * 如果修改后出现问题，请注意正负号的问题
 * @author cym
 */
class XYView(mContext: Context, mAttrs: AttributeSet) : View(mContext, mAttrs) {

    // 画笔
    private val mPaint = Paint()

    // 颜色
    private val mColorBlack = ContextCompat.getColor(mContext, R.color.black)
    private val mColorPurple = ContextCompat.getColor(mContext, R.color.purple_700)

    // X轴的偏移距离
    private var mOldOffsetX = 0F;
    private var mOffsetX = 0F
    private val mMaxOffsetX = 3000F
    private val mMinOffsetX = -3000F
    // X轴的偏移距离
    private var mOldOffsetY = 0F;
    private var mOffsetY = 0F
    private val mMaxOffsetY = 3000F
    private val mMinOffsetY = -3000F

    // 手指按下坐标
    private var mDownX = 0F
    private var mCurX = 0F
    private var mUpX = 0F
    private var mUpOffsetX = 0F
    private var mDownY = 0F
    private var mCurY = 0F
    private var mUpY = 0F
    private var mUpOffsetY = 0F

    // 速度计算器，用来计算惯性滑动的距离
    private val mVelocityTracker = VelocityTracker.obtain()
    // 模拟手指惯性滑动
    private val mScroller = OverScroller(mContext)

    /**
     * 回调
     */
    interface OnCallbackListener {
        fun onCallback(offsetX: Float, offsetY: Float)
        fun onCallback(offsetX: Float, offsetY: Float, upOffsetX: Float, upOffsetY: Float)
    }

    private var mOnCallbackListener: OnCallbackListener? = null

    fun setOnCallbackListener(onCallbackListener: OnCallbackListener) {
        mOnCallbackListener = onCallbackListener
    }

    /**
     * 重置方法
     */
    fun reset() {
        mUpX = 0F
        mUpY = 0F
        mOffsetX = 0F
        mOffsetY = 0F
        invalidate()
    }

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 画默认十字坐标轴
        mPaint.color = mColorBlack
        mPaint.strokeWidth = 5F
        // 主竖线
        val centerOffsetX = (mMaxOffsetX + mMinOffsetX) / 2F
        val lineX = (centerOffsetX - mOffsetX) + width / 2F
        canvas.drawLine(lineX, 0F, lineX, height.toFloat(), mPaint)
        // 主横线
        val centerOffsetY = (mMaxOffsetY + mMinOffsetY) / 2F
        val lineY = -(centerOffsetY - mOffsetY) + height / 2F
        canvas.drawLine(0F, lineY, width.toFloat(), lineY, mPaint)

        // 画点击位置的十字坐标轴
        if (mUpX > 0 && mUpY > 0) {
            mPaint.color = mColorPurple
            mPaint.strokeWidth = 2F
            // 点击竖线
            val upLineX = (mUpOffsetX - mOffsetX) + mUpX
            canvas.drawLine(upLineX, 0F, upLineX, height.toFloat(), mPaint)
            // 点击横线
            val upLineY = -(mUpOffsetY - mOffsetY) + mUpY
            canvas.drawLine(0F, upLineY, width.toFloat(), upLineY, mPaint)
        }

        // 每次onDraw后都进行回调
        if (mUpX == 0F && mUpY == 0F) {
            mOnCallbackListener?.onCallback(mOffsetX, mOffsetY)
        } else {
            mOnCallbackListener?.onCallback(mOffsetX, mOffsetY, mUpOffsetX + mUpX - width / 2F, mUpOffsetY - mUpY + height / 2F)
        }
    }

    /**
     * onTouch处理手指触碰事件
     * 报黄是因为没有重写performClick事件，不用管。这个事件是给盲人用的，没有影响，只是看起来不爽
     * https://www.youtube.com/watch?v=1by5J7c5Vz4
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mVelocityTracker.addMovement(event)
        when (event.action) {
            // 按下
            MotionEvent.ACTION_DOWN -> {
                mCurX = event.x
                mCurY = event.y
                mDownX = mCurX
                mDownY = mCurY
                // 按下时停止惯性滑动事件
                mScroller.forceFinished(true)
                return true
            }
            // 滑动
            MotionEvent.ACTION_MOVE -> {
                // 这里的 + - 控制坐标方向，自己改改试试
                // 注意下面抬手的地方 vX vY 要和这里的符号相反
                mOffsetX += (mCurX - event.x)
                mOffsetY -= (mCurY - event.y)
                mCurX = event.x
                mCurY = event.y
                // 滑动区域不能超过最大最小值
                if (mOffsetX < mMinOffsetX) {
                    mOffsetX = mMinOffsetX
                } else if (mOffsetX > mMaxOffsetX) {
                    mOffsetX = mMaxOffsetX
                }
                if (mOffsetY < mMinOffsetY) {
                    mOffsetY = mMinOffsetY
                } else if (mOffsetY > mMaxOffsetY) {
                    mOffsetY = mMaxOffsetY
                }
                invalidate()
                return true
            }
            // 抬起
            MotionEvent.ACTION_UP -> {
                // 计算速度
                mVelocityTracker.computeCurrentVelocity(1000)
                val vX = mVelocityTracker.xVelocity.toInt()
                val vY = mVelocityTracker.yVelocity.toInt()
                mVelocityTracker.clear()
                // 惯性滑动
                mScroller.fling(0, 0, -vX, vY, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE)
                // 保存抬手时的偏移，方便计算惯性滑动的距离
                mOldOffsetX = mOffsetX
                mOldOffsetY = mOffsetY
                // 如果滑动距离很小，说明是点击，保存点击的XY坐标
                if (abs(event.x - mDownX) < 10 && abs(event.y - mDownY) < 10) {
                    mUpX = event.x
                    mUpY = event.y
                    mUpOffsetX = mOffsetX
                    mUpOffsetY = mOffsetY
                }
                invalidate()
                return false
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * computeScroll处理惯性滑动事件
     */
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // 滑动中
            mOffsetX = mOldOffsetX + mScroller.currX
            mOffsetY = mOldOffsetY + mScroller.currY
            // 滑动区域不能超过最大最小值
            if (mOffsetX < mMinOffsetX) {
                mOffsetX = mMinOffsetX
            } else if (mOffsetX > mMaxOffsetX) {
                mOffsetX = mMaxOffsetX
            }
            if (mOffsetY < mMinOffsetY) {
                mOffsetY = mMinOffsetY
            } else if (mOffsetY > mMaxOffsetY) {
                mOffsetY = mMaxOffsetY
            }
            invalidate()
        }
    }
}