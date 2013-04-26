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
    private int mTopChildIndex;
    
    private static final int PADDING = 100;
    private static final float MAX_SCALE = 0.4f;
    
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
    private static final double NEIBOUR_ANGLE_RADIAN = 0.66667 * Math.PI;
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        Log.d(TAG, "onLayout............");
        int count = getChildCount();
        
        // calculate the dimensions of current layout
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
            double curAngle = (NEIBOUR_ANGLE_RADIAN) * i + mAngle;
            int centerX = (int) (Math.sin(curAngle) * mRadius);
            
            int childLeft = parentWidth / 2 - centerX - childWidth / 2;
            int childTop = (parentHeight - childHeight) / 2;
            
            // layout the child
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            
            // compute the scale
            float scale = 1 - (float) ((1 - Math.cos(curAngle)) * 0.5 * MAX_SCALE);
            
            // set the child's scale
//            child.setScaleX(scale);
//            child.setScaleY(scale);
            
            // scaleChild(child, scale);
            
            // compute the cosine value as a weight that inflects the vertical distance from the child to us
            float weight = (float) Math.cos(curAngle);
            mChildDrawingOrderMap.put(weight, i);

        }
        
        // Compute the child drawing order
        computeChildDrawingOrder();
    }
    
    
    private void scaleChild(View child, float scale) {
        int width = (int) (scale * child.getMeasuredWidth());
        int height = (int) (scale * child.getMeasuredHeight());
        child.setLayoutParams(new LayoutParams(width, height));
    }

    private void computeChildDrawingOrder() {
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
        
        mTopChildIndex = mChildDrawingOrder[mChildDrawingOrder.length - 1];
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i < mChildDrawingOrder.length) {
            return mChildDrawingOrder[i];
        }
        
        return super.getChildDrawingOrder(childCount, i);
    }

    private int mOldX = 0;
    private int mOldY = 0;
    private int mLastScroll = 0;
    @Override
    public void computeScroll() {
        
        if (mScroller.computeScrollOffset()) {    // scrolling is continuing
            // Get current x and y positions
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();
            
            int offset = currX - mOldX;
            
            mOldX = currX;
             
            Log.d(TAG, "currX = " + currX + ", " + "currY = " + currY);
            // Compute the scroll offset
            //mAngle = (float) ((((float) offset) / mRadius) % (Math.PI * 2)) * -1;
            //mAngle += ((float) offset) / mRadius * -1;
            float deltaAngle = ((float) Math.abs(offset)) / mRadius;
            if (currX < 0) { // To left
                mAngle += deltaAngle;
            } else {
                mAngle -= deltaAngle;
            }

            Log.d(TAG, "computeScroll: mAngle = " + mAngle + ", radius = " + mRadius);
            // Ask to redraw the layout
            requestLayout();
            invalidate(); 
        } else {    // scrolling is finished
            // handle the selection change
            Log.d(TAG, "Scrolling done...........");
//            if (mFling) {
//                double curAngle = (NEIBOUR_ANGLE_RADIAN) * mTopChildIndex + mAngle;
//                curAngle = curAngle % CIRCLE_RADIAN;
//                
//                float deltaAngle = (float) ((mAngle - NEIBOUR_ANGLE_RADIAN * mTopChildIndex) % CIRCLE_RADIAN);
//                int dx = (int) (deltaAngle * mRadius);
//                mScroller.startScroll(0, 0, dx, 0, 1000);
//                mFling = false;
//            }
        }

    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mFling = false;
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private static final double CIRCLE_RADIAN = Math.PI * 2;
    // Extends from the listener to monitor gestures
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            //Log.d(TAG,"onScroll: " + e1.toString() + e2.toString());
            mAngle %= CIRCLE_RADIAN;
            mAngle += distanceX / mRadius;
            Log.d(TAG,"onScroll: angle = " + mAngle + ", radius = " + mRadius);

            requestLayout();
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            Log.d(TAG, "onFling: " + e1.toString()+e2.toString());
            mFling = true;
            
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
