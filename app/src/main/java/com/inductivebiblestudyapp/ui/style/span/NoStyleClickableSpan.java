package com.inductivebiblestudyapp.ui.style.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/** Required to prevent click span styling while having clickable space. 
 * @version 0.4.0-20150726 */
public class NoStyleClickableSpan extends ClickableSpan {
	
	private final OnClickListener mListener;
	private boolean mIsEnabled = true;
	
	private ObjectEnabledCallback mEnabledCallback = null;
	
	public NoStyleClickableSpan(OnClickListener listener) {
		this.mListener = listener;
	}
	
	/**
	 * Sets the enabled callback for the span. Depending on the value, shows or hides 
	 * contents. Note that this overrides the functionality of {@link #setEnabled(boolean)}
	 * @param callback The callback or <code>null</code> to disable callback.
	 * @see #setEnabled(boolean)
	 */
	public void setEnabledCallback(ObjectEnabledCallback callback) {
		this.mEnabledCallback = callback;
	}
	
	public void setEnabled(boolean enabled) {
		this.mIsEnabled = enabled;
	}

	@Override
	public void onClick(View widget) {
		if (isEnabled()){
			mListener.onClick(widget);
		}
	}

    /** @return The value of whether the boolean or callback declares the span enabled. */
    protected boolean isEnabled() {
    	if (mEnabledCallback != null) {
			return mEnabledCallback.isObjectEnabled();
		}
		return mIsEnabled;
    }	
	
	@Override
	public void updateDrawState(TextPaint ds) {} //do not update
	
	public static interface OnClickListener {
		/**
	     * Performs the click action associated with this span.
	     */
	    public void onClick(View widget);
	}

}
