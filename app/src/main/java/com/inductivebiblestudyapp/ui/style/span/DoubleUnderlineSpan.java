/*
 * Copyright (C) 2011 Alex Kuiper <http://www.nightwhistler.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.os.Parcel;

/**
 * <p><b>Remember:</b> This span and its children must be applied at the paragraph level
 * in order to function. </p>
 * 
 * 
 * <p>Creates a double underline in either the given color or the text color. </p>
 * 
 * @author Jason Jenkins
 * @version 0.4.0-20150803
 */
public class DoubleUnderlineSpan extends AbstractCustomBackgroundSpan  {
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = DoubleUnderlineSpan.class
			.getSimpleName();

	/** If this is 0 (i.e. black alpha), is it unset. */
	protected final int mColor;	

	/** Same as calling {@link #DoubleUnderlineSpan(int, int, int)} with 0 color.
	 * @param start The starting character to underline
	 * @param end The ending character to underline
	 */
	public DoubleUnderlineSpan(int start, int end) {
		this(0, start, end);
	}
	
	/**
	 * @param color The hex color to box with. If 0 the text colour is used.
	 * @param start The starting character to underline
	 * @param end The ending character to underline
	 */
    public DoubleUnderlineSpan(int color, int start, int end) {
    	super(start, end);
        this.mColor = color;       
    }
    
    public DoubleUnderlineSpan(Parcel src) {
    	super(src);
        int[] values = new int[1]; 
		src.readIntArray(values);
		mColor = values[0];
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
    	int[] values = new int[]{mColor};
    	dest.writeIntArray(values);
    }
	
	@Override
	public int describeContents() {
		return 0;
	}

    
    ////////////////////////////////////////////////////////////////////////////////////////////////
	//// LineBackgroundSpan interface
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void drawBackgroundCustom(Canvas canvas, Paint paint, int left, int right,
			int top, int baseline, int bottom, CharSequence text, int start,
			int end, int lnum) {
		
		FontMetricsInt originalFontMetrics = paint.getFontMetricsInt();		
        
        int originalColor = paint.getColor();
        float originalStroke = paint.getStrokeWidth();        
        FontMetrics metrics = paint.getFontMetrics();
        
        Rect bounds = new Rect();
        paint.getTextBounds(text.toString(), start, end, bounds);
        
        if (mColor != 0) {
        	paint.setColor( mColor );
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(metrics.descent/5);
        
		top = top - (originalFontMetrics.top - originalFontMetrics.ascent) + 1;
        bottom = bottom - (originalFontMetrics.bottom - originalFontMetrics.descent);
        
        //top line
        int newBase = baseline +  originalFontMetrics.descent/2;
        int base2 = (int) (baseline + (bottom - baseline) * 0.6f);
        if (base2 == 0 || newBase == base2) {
        	base2 = bottom;
        }
        canvas.drawLine(left, newBase, right, newBase, paint);
        //bottom line
        canvas.drawLine(left, base2, right, base2, paint);
        

        paint.setColor(originalColor);
        paint.setStrokeWidth(originalStroke);
	}
	
	


	
}
