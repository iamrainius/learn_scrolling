package jing.app.scroll;

import android.content.Context;
import android.view.GestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class CircleLayout extends FrameLayout {

	private Scroller mScroller;
	private int mOriginX;
	private int mOriginY;
	
	public CircleLayout(Context context) {
		super(context);

	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int count = getChildCount();
		
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			// compute the child's position
			int childLeft = 0;
			int childTop = 0;
			int width = 0;
			int height = 0;
			
			// layout the child
			child.layout(childLeft, childTop, childLeft + width, childTop + height);
			
			// compute the scale
			float scale = 0.0f;
			
			// set the child's scale
			child.setScaleX(scale);
            child.setScaleY(scale);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {    // scrolling is continuing
		     // Get current x and y positions
		     int currX = mScroller.getCurrX();
		     int currY = mScroller.getCurrY();
		     // Compute the scroll offset
		     
		     // Ask to redraw the layout
		     requestLayout();
	         invalidate(); 
		 } else {    // scrolling is finished
			 // handle the selection change
		 }

	}
	// Extends from the listener to monitor gestures
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
		
	}

}
