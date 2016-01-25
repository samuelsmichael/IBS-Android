package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;
import android.text.style.LineHeightSpan;

/**
 * Provides a leading span to show the verse number in the left column, with spacing.
 * Additionally can ensure the line is of sufficient height to show the verse number.
 * 
 * @author Jason Jenkins
 * @version 0.10.0-20150803
 */
public class VerseNumberSpan implements LeadingMarginSpan, LeadingMarginSpan2, Parcelable,
	LineHeightSpan {
	
	private static final String MAX_VERSE_NUMBER = "176_"; //based on psalms 119


    private final String mVerseNumber;
    private final int mLeadWidth;
    private final int mPaddingWidth;
    private final float mTextSize;
    
    private final int mStart;

    private boolean mIsEnabled = true;

    private int mMinVerseHeight = 0;
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// End parcellable members
	////////////////////////////////////////////////////////////////////////////////////////////////

    private ObjectEnabledCallback mEnabledCallback = null;
    
    /**
     * 
     * @param verse The verse number 
     * @param start The starting index used to determine the first line
     * @param leading The leading padding
     * @param padding The right padding.
     * @param textSize The font size to use.
     */
    public VerseNumberSpan(String verse, int start, int leading, int padding, float textSize) {
        this.mVerseNumber = verse;
        this.mLeadWidth = leading;
        this.mPaddingWidth = padding;
        this.mTextSize = textSize;
        this.mStart = start;
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


    /** Sets the minimum verse height allowed for this verse item.
     * @param minHeight
     */
    public void setMinimumVerseHeight(int minHeight) {
    	this.mMinVerseHeight = minHeight;
    }
    
    /** Note that the span string must be re-applied in order for this to have effect. 
     * @param enabled <code>true</code> to set as visible, <code>false</code> to hide.
     * Default <code>true</code>.
     */
    public void setEnabled(boolean enabled) {
		this.mIsEnabled = enabled;
	}
    
    /** Calculates and returns the spans verse number height. */
    public int getNumberHeight(Paint p) {
	   
	   Paint.Style orgStyle = p.getStyle();
       float orgTextSize = p.getTextSize();
       
       p.setStyle(Paint.Style.FILL);
       p.setTextSize(mTextSize);
	   
       Rect bounds = new Rect();
       p.getTextBounds(MAX_VERSE_NUMBER, 0, MAX_VERSE_NUMBER.length(), bounds);
       final int height = bounds.bottom - bounds.top;
       
       p.setStyle(orgStyle);
       p.setTextSize(orgTextSize);
       
       if (!isSingleLine()) {
    	   return height * 2 + (int) p.getFontMetrics().leading;
       } 
       return height;
   }
    
    /** @return The value of whether the boolean or callback declares the span enabled. */
    protected boolean isEnabled() {
    	if (mEnabledCallback != null) {
			return mEnabledCallback.isObjectEnabled();
		}
		return mIsEnabled;
    }
    
    /** Determines if the number will run on two lines, such as being hypenated and more than 3
     * characters long.
     * @return <code>false</code> if two lines, <code>true</code> if one.
     */
    public boolean isSingleLine() {
		return mVerseNumber.length() < MAX_VERSE_NUMBER.length() || mVerseNumber.indexOf("-") < 0;
	}
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public int getLeadingMargin(boolean first) {
        return mLeadWidth + mPaddingWidth;
    }
    
	@Override
	public int getLeadingMarginLineCount() {
		return 1; //only allow first line
	}

	@Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout l) {

    	//Log.d("VerseNumber", String.format("text: %s, start: %d, end %d, first %d", text, start, end, first ? 1 : 0));
    	
    	if (first && start == mStart && isEnabled()) {
        	Paint.Style orgStyle = p.getStyle();
            float orgTextSize = p.getTextSize();
            
            p.setStyle(Paint.Style.FILL);
            p.setTextSize(mTextSize);
            
            float width = p.measureText(MAX_VERSE_NUMBER); 
                      
            if (isSingleLine()) { 
            	c.drawText(mVerseNumber, (mLeadWidth + x - width / 2) * dir, bottom - p.descent(), p);            	
            } else { //oh dear, we've gone hypenated
            	//we must split it
            	int hypen = mVerseNumber.indexOf("-");
            	String line1 = mVerseNumber.substring(0, hypen+1);
            	String line2 = mVerseNumber.substring(hypen+1);

            	c.drawText(line1, (mLeadWidth + x - width / 2) * dir, bottom - p.descent(), p);
            	//second line
            	c.drawText(line2, (mLeadWidth + x - width / 2) * dir, bottom - p.descent() + (bottom - top), p);
            	//Log.d("VerseNumberSpan", "hypenate: " + mVerseNumber);            	
            }
            
            p.setStyle(orgStyle);
            p.setTextSize(orgTextSize);
        }
    }
    
    /** Not to be stored; used only by {@link #chooseHeight(CharSequence, int, int, int, int, FontMetricsInt)} */
    private int mParagraphHeight = 0;

	@Override
	public void chooseHeight(CharSequence text, int start, int end,
			int spanstartv, int v, FontMetricsInt fm) {
		if (!isEnabled()) { //do not do work if not required
			return;
		}
		Spanned spannedText = (Spanned) text;
		
		final int spanStart = spannedText.getSpanStart(this);
		final int spanEnd = spannedText.getSpanEnd(this);
		
		if (spanStart == start) {
			mParagraphHeight = 0;
		}		
		//add height of this line
		mParagraphHeight += fm.bottom - fm.top;
		
		
		if (spanEnd == end && mParagraphHeight < mMinVerseHeight) {
			int padding = mMinVerseHeight - mParagraphHeight;
			fm.descent += padding;
			fm.bottom += padding;
		}
	}


    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// Parcellable interface
	////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*public static final Parcelable.Creator<VerseNumberSpan> CREATOR = new Parcelable.Creator<VerseNumberSpan>() {
		public VerseNumberSpan createFromParcel(Parcel in) {
		    return new VerseNumberSpan(in);
		}
		
		public VerseNumberSpan[] newArray(int size) {
		    return new VerseNumberSpan[size];
		}
	};
*/
    
    public VerseNumberSpan(Parcel src) {
        int[] values = new int[4]; 
		src.readIntArray(values);				

        this.mLeadWidth = values[0];
        this.mPaddingWidth = values[1];
        this.mStart = values[2];
        this.mIsEnabled = values[3] != 0;
		
		this.mVerseNumber = src.readString();
        this.mTextSize = src.readFloat();
        
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	int[] values = new int[]{
    			mLeadWidth, mPaddingWidth, 
    			mStart, 
    			mIsEnabled ? 1 : 0};
    	dest.writeIntArray(values);
    	
    	dest.writeString(mVerseNumber);
    	dest.writeFloat(mTextSize);
    }

	@Override
	public int describeContents() {
		return 0;
	}


}
