package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.StaticLayout;
import android.text.Layout.Alignment;
import android.text.TextPaint;
import android.text.style.LineBackgroundSpan;
 
/**
 * <p><b>Remember:</b> This span and its children must be applied at the paragraph level
 * in order to function. </p>
 * <p>
 * Creates an manageable means to draw background elements for this project.
 * Based heavily on: https://github.com/NightWhistler/HtmlSpanner/blob/master/src/main/java/net/nightwhistler/htmlspanner/spans/BorderSpan.java
 * </p>
 * 
 * Helpful reading: 
 * - http://flavienlaurent.com/blog/2014/01/31/spans/
 * - http://www.informit.com/articles/article.aspx?p=2066699&seqNum=3
 * - https://github.com/slapperwan/gh4a/blob/master/src/com/gh4a/widget/EllipsizeLineSpan.java
 * - https://github.com/NightWhistler/HtmlSpanner/blob/master/src/main/java/net/nightwhistler/htmlspanner/spans/BorderSpan.java
 * - http://stackoverflow.com/questions/7549182/android-paint-measuretext-vs-gettextbounds
 * 
 * @author Jason Jenkins
 * @version 2.0.0-20150803
 */
abstract public class AbstractCustomBackgroundSpan implements Parcelable, LineBackgroundSpan {
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = AbstractCustomBackgroundSpan.class
			.getSimpleName();
	
	protected final int mStart;
	protected final int mEnd;
	
	protected boolean mIsCentered = false;
	
	protected OnMeasureCallback mMeasureCallback = null;
	
	protected ObjectEnabledCallback mEnabledCallback = null;
 
	/**
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
    public AbstractCustomBackgroundSpan(int start, int end) {
    	  this.mStart = start;
    	  this.mEnd = end;
    }
    
    public AbstractCustomBackgroundSpan(Parcel src) {
        int[] values = new int[2]; 
		src.readIntArray(values);
		mStart = values[0];
		mEnd = values[1];
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	int[] values = new int[]{mStart, mEnd};
    	dest.writeIntArray(values);
    }
    
	@Override
	public int describeContents() {
		return 0;
	}
	
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    /** If set <code>true</code>, the alignment is ignored and the content assumed centered. */ 
    public void setCentered(boolean centered) {
    	this.mIsCentered = centered;
    }
  
    
    /** LEts the span accurately measure its text using the current text paint. */
    public void setOnMeasureCallback(OnMeasureCallback callback) {
    	this.mMeasureCallback = callback;
    }
   

    /** Lets the span know when to display and when not to. */
    public void setEnabledCallback(ObjectEnabledCallback callback) {
    	this.mEnabledCallback = callback;
    }
 
      @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
    	  final String textAsString = text.toString();
    	  final String textLine = textAsString.substring(start, end).trim();
    	  if (mStart > end || mEnd < start || textLine.isEmpty()){
    		  return;
    	  }
    	  if (mEnabledCallback != null && !mEnabledCallback.isObjectEnabled()) {
    	  	return;
    	  }
    	  
    	  boolean isRightAligned = false;    	  
    	  int marginLeft = 0;
    	  int marginRight = 0;
    	  
    	  if (mMeasureCallback != null) {
    		  isRightAligned = mMeasureCallback.isRightAligned(start, end);
    		  marginLeft = mMeasureCallback.getMarginLeft(start, end);
    		  marginRight = mMeasureCallback.getMarginRight(start, end);
    	  }
    	   
    	  final int fullWidth = right - left;
    	  final int textWidth = (int) measureTextWidth(p, text, start, end);
       
    	  
    	  final Align align = p.getTextAlign();
    	
    	  left += marginLeft;
    	  right -= marginRight;
    	
    	  if (align == Align.LEFT && !mIsCentered && !isRightAligned) {
    		  if (end >= mEnd) { //based on left
    			  right = left +  (int) measureTextWidth(p, text, start, mEnd);
    		  } else {
    			  right = left + textWidth;
    		  }
    		  if (start <= mStart && mStart <= end) {    	    	  
    			left += measureTextWidth(p, text, start, mStart);
    		  }
    		
    	  } else if (align == Align.RIGHT && !mIsCentered || isRightAligned) { //experimental 
    		  //Remember: right->left text widths are sometimes negative
    		  if (start <= mStart && mStart <= end) { //based on right 	    	  
    			  left = right - (int) Math.abs(measureTextWidth(p, text, mStart, end)); 
    		  } else {
    			  left = right - Math.abs(textWidth);
    		  }
    		  if (end >= mEnd) {
    			  right -= Math.abs(measureTextWidth(p, text, mEnd, end));
    		  } 
    		
    	  } else { //Align.CENTER
    		  final int margins = fullWidth - textWidth;
    		  if (start <= mStart && mStart <= end) {
    			  left += measureTextWidth(p, text, start, mStart);
    		  }
    		  if (end >= mEnd) { 
    			  right -= measureTextWidth(p, text, mEnd, end);
    		  }
    		  left += margins/2;
    		  right -= margins/2;
    	  }
    	  
    	/*FontMetricsInt originalFontMetrics = p.getFontMetricsInt();
    	  Log.d(CLASS_NAME, 
    	  		String.format("drawBackground: oascent(%d), odescent(%d), otop(%d), obottom(%d), top(%d), bottom(%d), baseline(%d), lnum(%d)", 
    	  				originalFontMetrics.ascent, originalFontMetrics.descent, 
    	  				originalFontMetrics.top, originalFontMetrics.bottom,  
    	  				top, bottom, baseline, lnum)
    	  				);   */     
      
     
       /*if (start <= mStart && mStart <= end) {
    	   Log.d(CLASS_NAME, String.format("substring(%s), fullWidth(%d), textWidth(%d), left(%d), right(%d)", 
    	  		   text.toString().substring(start, end), fullWidth, textWidth, left, right));
    	   Log.d(CLASS_NAME, String.format("Adjusting left: start(%d), end(%d), mStart(%d), mEnd(%d)", 
 				start, end, mStart, mEnd) );
    	   left += p.measureText(text, start, mStart);
       }
       if (end >= mEnd) {
    	   Log.d(CLASS_NAME, String.format("substring(%s), fullWidth(%d), textWidth(%d), left(%d), right(%d)",
    	  		   text.toString().substring(start, end), fullWidth, textWidth, left, right));
    	   Log.d(CLASS_NAME, String.format("Adjusting right: start(%d), end(%d), mStart(%d), mEnd(%d)", 
 				start, end, mStart, mEnd));
    	   right -= p.measureText(text, mEnd, end);
       }*/
       
  		drawBackgroundCustom(c, p, left, right, top, baseline, bottom, text, start, end, lnum);
    }
      
    /** Passes on the adjusted values to account for margins, alignment and spacing. */
    abstract public void drawBackgroundCustom(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum);
      
    
    
      ////////////////////////////////////////////////////////////////////////////////////////////////
      //// Utility methods
      ////////////////////////////////////////////////////////////////////////////////////////////////
 
      
      /** @return An slightly more accurate measurement of the text. */
      protected float measureTextWidth(Paint p, CharSequence text, int start, int end) {
    	  /* p.getTextBounds(text, start, end, sBounds);
    	  p.getTextPath(text, start, end, 0.0f, 0.0f, sPath);
    	  sPath.computeBounds(sFBounds, true);
    	  float width1 = sBounds.left + sBounds.width();
    		float width2 = p.measureText(text, start, end);
    		if (width2 - width1 > 5) { //this indicates the large one is inaccurate
    		return width1;
    		}*/
    	 
    	  //width is moot as we are always measuring one line
		  StaticLayout staticLayout = new StaticLayout(text, start, end, (TextPaint) p, 
		  				Integer.MAX_VALUE, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		  return staticLayout.getLineWidth(0);
      }
      
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces 
	////////////////////////////////////////////////////////////////////////////////////////////////
      
      /** Provides the means for the span to correctly update its size attributes.
       * @version 0.1.0-20150803
       */
      public static interface OnMeasureCallback {
    	  /** The right margin of the given line. */
    	  int getMarginRight(int start, int end);
    	  /** The left margin of the given line. */
    	  int getMarginLeft(int start, int end);
    	  /** Whether the given line is right aligned. */
    	  boolean isRightAligned(int start, int end);
      }
}