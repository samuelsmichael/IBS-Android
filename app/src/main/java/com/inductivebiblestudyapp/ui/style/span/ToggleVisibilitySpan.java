
package com.inductivebiblestudyapp.ui.style.span;



import android.graphics.Paint.FontMetricsInt;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.LineHeightSpan;
import android.text.style.RelativeSizeSpan;

/** 
 * Creates a span that can toggle the visibility of the enclosed span, using size.
 * Based heavily on the source of {@link RelativeSizeSpan}. 
 * Note that the span string must be re-applied in order for this class to have its 
 * desired effect.
 * 
 * @author Jason Jenkins
 * @version 0.3.1-20150902
 */
public class ToggleVisibilitySpan extends RelativeSizeSpan implements LineHeightSpan {

	private static final float mInvisibleSize = 0.000000001f; //1 / billion
	
	private boolean mIsVisible = true;	
	private boolean mHideTopPadding = true;
	
	private ObjectEnabledCallback mVisibilityCallback = null;
	
	public ToggleVisibilitySpan() {
		super(1.0f);
	}
	
	/**
	 * Sets the visibility callback for the span. Depending on the value, shows or hides 
	 * contents. Note that this overrides the functionality of {@link #setVisibility(boolean)}
	 * @param callback The callback or <code>null</code> to disable callback.
	 * @see #setVisibility(boolean)
	 */
	public void setVisibilityCallback(ObjectEnabledCallback callback) {
		this.mVisibilityCallback = callback;
	}
	
	/** Sets whether or not to hide the top padding & ascent, default <code>true</code>.
	 * @param hidePadding <code>true</code> to hide it when changing visibility, 
	 * <code>false</code> to keep padding and hide text only.
	 */
	public void setHideTopPadding(boolean hidePadding) {
		this.mHideTopPadding = hidePadding;
	}
	
	public void setVisibility(boolean visible) {
		this.mIsVisible = visible;
	}

	public boolean toggleVisibility() {
		mIsVisible = !mIsVisible;
		return mIsVisible;
	}
	
	/** @return Whether the boolean or callback declares this block visible. */
	protected boolean isVisible() {
		if (mVisibilityCallback != null) {
			return mVisibilityCallback.isObjectEnabled();
		}
		return mIsVisible;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public float getSizeChange() {
		return isVisible() ? 1.0f : mInvisibleSize;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		updateTextPaint(ds);
	}

	@Override
	public void updateMeasureState(TextPaint ds) {
		updateTextPaint(ds);
	}
	
	@Override
	public void chooseHeight(CharSequence text, int start, int end,
			int spanstartv, int v, FontMetricsInt fm) {
		//Spanned spannedText = (Spanned) text;		
		//final int spanStart = spannedText.getSpanStart(this);
		
		if (!isVisible()) { //remove ALL height
			if (mHideTopPadding) { //only remove top if allowed
				fm.ascent = 0;
				fm.top = 0;		
			} else {
				//nothing here
			}
			fm.descent = 0;
			fm.bottom = 0;
		}

	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Parcelable interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	
    protected void updateTextPaint(TextPaint ds) {
    	if (isVisible()) {
    		ds.setTextSize(ds.getTextSize() * 1.0f);
        	ds.setAlpha(ds.getAlpha());
    	} else {
    		ds.setTextSize(ds.getTextSize() * mInvisibleSize);
    		ds.setAlpha(0); //ensure the text is not visible
    	}
    }


    public ToggleVisibilitySpan(Parcel src) {
    	super(1.0f);
        boolean[] values = new boolean[2];
        src.readBooleanArray(values);
        
        mIsVisible = values[0];
        mHideTopPadding = values[1];
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    	boolean[] values = new boolean[]{mIsVisible, mHideTopPadding};
    	dest.writeBooleanArray(values);
    }	
}

