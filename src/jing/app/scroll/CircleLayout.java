package jing.app.scroll;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class CircleLayout extends FrameLayout {

    private static final float DEFAULT_ORIGIN_X = 0.5f;
    private static final float DEFAULT_ORIGIN_Y = 0.5f;
    
    private Scroller mScroller;
    private GestureDetectorCompat mGestureDetector;
    private boolean mFling = false;

    private float mAngle = 0.0f;
    private int mRadius = 0;
    
    public CircleLayout(Context context) {
        super(context);
        init(context);
    }
    
    public CircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public CircleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetectorCompat(context, new MyGestureListener());
        mScroller = new Scroller(context);
        setClickable(true);
        setFocusableInTouchMode(true);
    }

    
    private static final int PADDING = 200;

    private static final String TAG = "CircleLayout";
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        Log.d(TAG, "onLayout............");
        int count = getChildCount();
        
        int parentWidth = right - left;
        int parentHeight = bottom - top;
        mRadius = parentWidth / 2 - PADDING;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            // compute the child's position
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            
            // calculate the center
            double curAngle = (0.66667 * Math.PI) * i + mAngle;
            int centerX = (int) (Math.sin(curAngle) * mRadius);
            
            int childLeft = parentWidth / 2 - centerX - childWidth / 2;
            int childTop = (parentHeight - childHeight) / 2;
            
            // layout the child
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            
            // compute the scale
            final float MAX_SCALE = 0.2f;
            float scale = 1 - (float) ((1 - Math.cos(curAngle)) * 0.5 * MAX_SCALE);
            
            // set the child's scale
            child.setScaleX(scale);
            child.setScaleY(scale);

        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void computeScroll() {
        
        if (mScroller.computeScrollOffset()) {    // scrolling is continuing
             // Get current x and y positions
             int currX = mScroller.getCurrX();
             int currY = mScroller.getCurrY();
             Log.d(TAG, "currX = " + currX + ", " + "currY = " + currY);
             // Compute the scroll offset
             mAngle = (float) (Math.abs(((float) currX) / mRadius) % (Math.PI * 2));
             Log.d(TAG, "computeScroll: mAngle = " + mAngle);
             // Ask to redraw the layout
             requestLayout();
             invalidate(); 
         } else {    // scrolling is finished
             // handle the selection change
         }

    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mFling = false;
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    // Extends from the listener to monitor gestures
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            Log.d(TAG,"onScroll: " + e1.toString() + e2.toString());
            mAngle += distanceX / mRadius;
            
            requestLayout();
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
            mFling = true;
            //mAngle = 0.0f;
            mScroller.fling(0, 0, (int) velocityX, (int) velocityY, -10000, 10000, -10000, 10000);
            invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG,"onDown: " + e.toString()); 
            mScroller.forceFinished(true);
            return true;
        }
        
    }

}
