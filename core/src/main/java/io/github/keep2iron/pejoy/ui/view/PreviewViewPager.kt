package io.github.keep2iron.pejoy.ui.view

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

class PreviewViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs) {

    private var onClickListener: OnClickListener? = null

    private val gestureDetector: GestureDetectorCompat

    init {
        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onClickListener?.onClick(this@PreviewViewPager)
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}