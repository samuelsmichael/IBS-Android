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
import android.os.Parcel;

/**
 * 
 * <p><b>Remember:</b> This span and its children must be applied at the paragraph level
 * in order to function. </p>
 * 
 * <p>Create a box around text of given colour or of the text colour.</p>
 * 
 * 
 * @author Jason Jenkins
 * @version 0.5.0-20150803
 */
public class BorderSpan extends AbstractCustomBackgroundSpan  {
	
	/** If this is 0 (i.e. black alpha), is it unset. */
	protected final int mColor;	

	/** Same as calling {@link #BorderSpan(int, int, int)} with 0 color.
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
	public BorderSpan(int start, int end) {
		this(0, start, end);
	}
	
	/**
	 * @param color The hex color to box with. If 0 the text colour is used.
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
    public BorderSpan(int color, int start, int end) {
    	super(start, end);
        this.mColor = color;
    }
    
    public BorderSpan(Parcel src) {
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
			int originalColor = paint.getColor();
	        FontMetricsInt originalFontMetrics = paint.getFontMetricsInt();

	        if (mColor != 0) {
	        	paint.setColor( mColor );
	        }
	       

			top =  baseline +  originalFontMetrics.ascent -  originalFontMetrics.descent/2;
	        bottom =  baseline +  originalFontMetrics.descent/2;
	        

	        /*Log.d("BorderSpan", 
	        		String.format("drawBackground: ascent(%d), descent(%d), top(%d), bottom(%d), top(%d), bottom(%d)", 
	        				originalFontMetrics.ascent, originalFontMetrics.descent, 
	        				originalFontMetrics.top, originalFontMetrics.bottom,
	        				top, bottom)
	        				);
	        Log.d("BorderSpan", 
	        		String.format("drawBackground: start(%d), end(%d), mStart(%d), mEnd(%d)", 
	        				start, end, mStart, mEnd)
	        				);*/
	        
	        drawBorder(canvas, paint, left, right, top, bottom, text, start, end);


	        paint.setColor(originalColor);
	}
	


	/** Calculates and draws border. */
	protected void drawBorder(Canvas canvas, Paint paint, int left, int right,
			int top, int bottom, CharSequence text, int start, int end) {

        float originalStrokeWidth = paint.getStrokeWidth();
                

        FontMetrics metrics = paint.getFontMetrics();

        paint.setStrokeWidth(metrics.descent/8);
        paint.setStyle(Paint.Style.STROKE);

        drawRectangle(canvas, paint, left, right, top, bottom);

        paint.setStrokeWidth(originalStrokeWidth);
	}

	/** Simple method for drawing border box. */
	static protected void drawRectangle(Canvas canvas, Paint paint, int left, int right,
			int top, int bottom) {
		float originalStroke = paint.getStrokeWidth();
		paint.setStrokeWidth(originalStroke * 2);
		
		canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(right, top, right, bottom, paint);
        
        paint.setStrokeWidth(originalStroke);
	}

	
}
