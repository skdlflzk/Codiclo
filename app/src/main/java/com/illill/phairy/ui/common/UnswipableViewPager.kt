package com.illill.phairy.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.MotionEventCompat
import androidx.viewpager.widget.ViewPager
import org.koin.standalone.KoinComponent


class UnswipableViewPager : ViewPager, KoinComponent {
    private val TAG = this@UnswipableViewPager.javaClass.simpleName


//    val clientViewModel by inject<ClientViewModel>()

    val enabled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var mCurrentPagePosition = 0
    fun reMeasureCurrentPage(position: Int) {
        mCurrentPagePosition = position
        requestLayout()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (enabled) {
            super.onInterceptTouchEvent(ev)
        } else {
            if (MotionEventCompat.getActionMasked(ev) == MotionEvent.ACTION_MOVE) {
                // ignore move action
            } else {
                if (super.onInterceptTouchEvent(ev)) {
                    super.onTouchEvent(ev)
                }
            }
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (enabled) {
            super.onTouchEvent(ev)
        } else {
            MotionEventCompat.getActionMasked(ev) != MotionEvent.ACTION_MOVE && super.onTouchEvent(ev)
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val hm: Int
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            var height = 0
            (0 until childCount).forEach { i ->
                val child = getChildAt(i)
                child?.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                val h = child.measuredHeight
                if (h > height) height = h
            }

            hm = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        } else {
            hm = heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, hm)
    }

}