package com.inductivebiblestudyapp.ui.style.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/** Required to give personal click span styling while having clickable space. 
 * @version 0.1.0-20150819 */
public class MyStyledClickableSpan extends ClickableSpan {
	
	private final OnClickListener mListener;
		
	public MyStyledClickableSpan(OnClickListener listener) {
		this.mListener = listener;
	}
	
	
	@Override
	public void onClick(View widget) {
		mListener.onClick(widget);
	}

    
	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setUnderlineText(true);
	} 
	
	public static interface OnClickListener {
		/**
	     * Performs the click action associated with this span.
	     */
	    public void onClick(View widget);
	}

}
