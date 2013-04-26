package jing.app.scroll;

import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class CircleLayout extends FrameLayout {

    private Scroller mScroller;
    private GestureDetectorCompat mGestureDetector;
    
    private boolean mFling = false;

    private float mAngle = 0.0f;
    private int mRadius = 0;
    
    private final int[] mChildDrawingOrder = { 0, 1, 2 };
    private final HashMap<Float, Integer> mChildDrawingOrderMap = new HashMap<Float, Integer>();
    
    private static final int PADDING = 250;
    
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
        setChildrenDrawingOrderEnabled(true);
        mGestureDetector = new GestureDetectorCompat(context, new MyGestureListener());
        mScroller = new Scroller(context);
        setClickable(true);
        setFocusableInTouchMode(true);
    }

    
    private static final String TAG = "CircleLayout";
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        Log.d(TAG, "onLayout............");
        int count = getChildCount();
        
        int parentWidth = right - left;
        int parentHeight = bottom - top;
        mRadius = parentWidth / 2 - PADDING;
        mChildDrawingOrderMap.clear();
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
            
            // compute the cosine value as a weight that inflects the vertical distance from the child to us
            float weight = (float) Math.cos(curAngle);
            mChildDrawingOrderMap.put(weight, i);

        }
        
        // Compute the child drawing order
        // Just sort the keys of the map
        Set<Float> keySet = mChildDrawingOrderMap.keySet();
        Float[] keys = new Float[keySet.size()];
        keys = keySet.toArray(keys);
        // A insertion sort algorithm
        for (int i = 1; i < keys.length; i++) {
            int k = i;
            for (int j = i - 1; j >= 0; j--) {
                if (keys[k] < keys[j]) {
                    float tmp = keys[k];
                    keys[k] = keys[j];
                    keys[j] = tmp;
                    k = j;
                }
            }
        }
        
        // Set the the order which we need
        for (int i = 0; i < mChildDrawingOrder.length; i++) {
            mChildDrawingOrder[i] = mChildDrawingOrderMap.get(keys[i]);
        }
    }
    
    
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i < mChildDrawingOrder.length) {
            return mChildDrawingOrder[i];
        }
        
        return super.getChildDrawingOrder(childCount, i);
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
             Log.d(TAG, "Scrolling done...........");
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
