package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Paint.FontMetricsInt;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;
import android.text.style.LineHeightSpan;

/**
 * Allows the bottom and top of a given block to be vertically padded by a given amount.
 * This amount is added to the ascent/top & decent/bottom of the first and last lines, 
 * respectively.
 * @author Jason Jenkins
 * @version 0.1.1-20150724
 */
public class VerticalPaddingSpan implements LineHeightSpan, Parcelable {
	
	private final int mTopPadding;
	private final int mBottomPadding;
	
	public VerticalPaddingSpan(int topPadding, int bottomPadding) {
		this.mTopPadding = topPadding;
		this.mBottomPadding = bottomPadding;
	}
	
	

	@Override
	public void chooseHeight(CharSequence text, int start, int end,
			int spanstartv, int v, FontMetricsInt fm) {
		Spanned spannedText = (Spanned) text;
		
		final int spanStart = spannedText.getSpanStart(this);
		final int spanEnd = spannedText.getSpanEnd(this);
		
		if (spanStart == start) {
			fm.ascent -= mTopPadding;
			fm.top -= mTopPadding;
			fm.descent = 0;
			fm.bottom = 0;
		}
		
		if (spanEnd == end) {
			fm.descent += mBottomPadding;
			fm.bottom += mBottomPadding;
		}

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Parcelable interface
	////////////////////////////////////////////////////////////////////////////////////////////////
	

    public VerticalPaddingSpan(Parcel src) {
        int[] values = new int[2];
        src.readIntArray(values);
        
        mTopPadding = values[0];
        mBottomPadding = values[1];
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    	int[] values = new int[]{mTopPadding, mBottomPadding};
    	dest.writeIntArray(values);
    }

}
