package com.illill.phairy.ui.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TutorialView extends FrameLayout {
    private static final float RADIUS = 50;

    private Paint mBackgroundPaint;
    private float mCx = -1;
    private float mCy = -1;

    private int mTutorialColor = Color.parseColor("#D20F0F0F");


    public TutorialView(Context context) {
        super(context);
        init();
    }

    public TutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TutorialView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCx = event.getX();
        mCy = event.getY();

//        if (rect.contains((int) mCx, (int) mCy)) {
//            this.setVisibility(View.GONE);
            Log.d("onTouchEvent: ", "onTouchEvent: x = "+mCx+", y = " + mCy);
            Log.d("onTouchEvent: ", "onTouchEvent: rect = "+rect.toShortString() );
//        }
        invalidate();
        return false;
    }

    Rect rect=null;
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mTutorialColor);
        if (rect!=null) { // && rect.contains((int) mCx, (int) mCy)

            canvas.drawCircle((rect.left+rect.right)/2, (rect.top+rect.bottom)/2, rect.height()/2, mBackgroundPaint);
        }
    }


   public void hole(Rect rect) {
        this.rect = rect;
        invalidate();
    }
}