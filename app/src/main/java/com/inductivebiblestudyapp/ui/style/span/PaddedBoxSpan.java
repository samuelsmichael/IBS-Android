package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

/**
 * Creates an empty padding block in the front of the selected text. Note that this must be applied to
 * a non-0 string (at least one character). If one calls {@link #restoreStyles(SpannableString, int, int)}, 
 * it will attempt to restore the underline styles but this is still experimental.
 * @author Jason Jenkns
 * @version 0.3.0-20150807
 */
public class PaddedBoxSpan extends  ReplacementSpan implements LineHeightSpan {

	protected final int mHeight;
	protected final int mWidth;
	

	protected final ObjectEnabledCallback mEnabledCallback;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End finals
	////////////////////////////////////////////////////////////////////////////////////////////////
	
		
	protected float mCurrHeightOffset = 0;
	
	/** 0 if unset. */
	protected int mColor;
	protected boolean mIsUnderlined;
	protected boolean mIsStrikeThrough;
	
	protected PreserveHeightCallback mHeightCallback = null;
	
	/**
	 * 
	 * @param width The width to make the box
	 * @param height The max height to set
	 * @param callback
	 */
	public PaddedBoxSpan(int width, int height, ObjectEnabledCallback callback) {
		mWidth = width;
		mHeight = height;
		mEnabledCallback = callback;
	}
	
	
	/** Attempts to calculate the styles it needs to preserve.
	 * Currently preserves: 
	 *  {@link AbstractCustomBackgroundSpan}, {@link ForegroundColorSpan}, 
	 *  {@link StrikethroughSpan}, {@link StyleSpan} {@link UnderlineSpan}  
	 * @param width
	 * @param height
	 * @param spanString
	 * @param spanStart
	 * @param spanEnd
	 * @return
	 */
	public PaddedBoxSpan restoreStyles(SpannableString spanString, int spanStart, int spanEnd)  {		
		int color = 0;
		ForegroundColorSpan colors[] = spanString.getSpans(spanStart, spanEnd, ForegroundColorSpan.class);
		if (colors.length > 0) {
			color = colors[colors.length-1].getForegroundColor(); //get top color
			if (color == 0) {
				color = 1; //shh, no one will be able to tell the difference
			}
		}
		this.mColor = color;		
		this.mIsUnderlined = spanString.getSpans(spanStart, spanEnd, UnderlineSpan.class).length > 0;
		this.mIsStrikeThrough = spanString.getSpans(spanStart, spanEnd, StrikethroughSpan.class).length > 0;
		
		return this;
	}
	
	/** Sets a callback to decide whether to reset the height or preserve it.
	 * Set to <code>null</code> to remove. */
	public void setPreserveHeightCallback(PreserveHeightCallback callback) {
		this.mHeightCallback = callback;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void chooseHeight(CharSequence text, int start, int end,
			int spanstartv, int v, FontMetricsInt fm) {
		if (mEnabledCallback != null && !mEnabledCallback.isObjectEnabled()) {
			return; //do no work if not enabled.
		}
		
		if (mHeightCallback != null && mHeightCallback.preserveHeight(text, start, end, spanstartv, v, fm)) {
			//again, do no work for this line
			return;
		}

		final Spanned spannedText = (Spanned) text;
		final int spanStart = spannedText.getSpanStart(this);
		
		if (spanStart >= start && spanStart < end) {
			final int height = fm.descent - fm.ascent; 			 
			if (height < mHeight) {
				if (mCurrHeightOffset == 0) { //cache height
					mCurrHeightOffset = (float) (mHeight - height);
				}
				fm.ascent -= mCurrHeightOffset; 
				fm.top -= mCurrHeightOffset; //add more to top than bottom
			} else if (	height > mHeight) {
				fm.ascent = -mHeight;
				fm.top = -mHeight;
			}
		}
	}

	@Override
	public int getSize(Paint paint, CharSequence text, int start, int end,
			FontMetricsInt fm) {
		if (mEnabledCallback != null && !mEnabledCallback.isObjectEnabled()) {
			return (int) paint.measureText(text, start, end);
		}
		return (int) paint.measureText(text, start, end) + mWidth;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			float x, int top, int y, int bottom, Paint paint) {
		if (mEnabledCallback != null && !mEnabledCallback.isObjectEnabled()) {
			return; //do no work if not enabled.
		}
		
		paint.setUnderlineText(mIsUnderlined);
		paint.setStrikeThruText(mIsStrikeThrough);
		if (mColor != 0) {
			paint.setColor(mColor);
		}		
        canvas.drawText(text, start, end, mWidth + x, y, paint);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Used to preserve the height of a given line(s) 
	 * @version 0.1.0-20150807
	 * */
	public static interface PreserveHeightCallback {
		/** @return <code>true</code> if to preserve this lines height, <code>false</code> to change it. */
		public boolean preserveHeight(CharSequence text, int start, int end,
				int spanstartv, int v, FontMetricsInt fm);
	}
	
}
