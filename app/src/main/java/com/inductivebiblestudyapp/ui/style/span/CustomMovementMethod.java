package com.inductivebiblestudyapp.ui.style.span;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.TextView;

//http://stackoverflow.com/questions/15836306/can-a-textview-be-selectable-and-contain-links
/**
 * Ensures that the given ClickableSpan will remain text selectable.
 * @author Jason Jenkins
 * @version 0.2.1-20150720
 */
public class CustomMovementMethod extends LinkMovementMethod {
	
	private final OnTouchListener mTouchListener;
	
	/**
	 * 
	 * @param touchListener Used to position the anchor.
	 */
	public CustomMovementMethod(OnTouchListener touchListener) {
		mTouchListener = touchListener;
	}
	
	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
			MotionEvent event) {
		boolean consumed = mTouchListener.onTouch(widget, event);
		if (consumed) {
			return true;
		}
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) { 
			//based on source, should prevent touch selection
           return true;
        }
		
		return super.onTouchEvent(widget, buffer, event);
	}
	
    @Override
    public boolean canSelectArbitrarily () {
        return true;
    }
   

   /* @Override
    public void initialize(TextView widget, Spannable text) {
        //Selection.setSelection(text, text.length());
    	//Prevent selections
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
       if ((dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0) {
           if (view.getLayout() == null) {
               // This shouldn't be null, but do something sensible if it is.
               //Selection.setSelection(text, text.length());
           }
       } else {
           //Selection.setSelection(text, text.length());
       }
    }*/
}