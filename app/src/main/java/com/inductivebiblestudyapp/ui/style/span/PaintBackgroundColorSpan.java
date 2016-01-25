package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetricsInt;
import android.text.style.BackgroundColorSpan;
 
/**
 * This work around was created as manually drawn paint do not seem to work with
 * {@link BackgroundColorSpan}. This may be due to this bug:
 *  -https://code.google.com/p/android/issues/detail?id=172001
 * @author Jason Jenkins
 * @version 0.3.0-20150727
 */
public class PaintBackgroundColorSpan extends AbstractCustomBackgroundSpan {
	
	private int mColor;    
    private Rect mBgRect;    
 
	/**
	 * @param color The hex color to work
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
    public PaintBackgroundColorSpan(int color,  int start, int end) {
        super(start, end);
        mColor = color;
        mBgRect = new Rect();

    }
    

	@Override
	public void drawBackgroundCustom(Canvas c, Paint p, int left, int right,
			int top, int baseline, int bottom, CharSequence text, int start,
			int end, int lnum) {
		final int paintColor = p.getColor();
		final FontMetricsInt originalFontMetrics = p.getFontMetricsInt();
		
		top = baseline +  originalFontMetrics.ascent - (originalFontMetrics.descent + originalFontMetrics.leading);
		
		 // Draw the background
        mBgRect.set(left,
                top,
                right,
                bottom);
        
        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);
        c.drawRect(mBgRect, p);
        
        p.setColor(paintColor);
	}
}