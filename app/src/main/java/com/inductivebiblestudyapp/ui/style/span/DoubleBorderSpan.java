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
import android.graphics.Paint.FontMetricsInt;

/**
 * <p><b>Remember:</b> This span and its children must be applied at the paragraph level
 * in order to function. </p>
 * <p>Creates a double box around the text using the given colour or of the text colour.</p>
 * @author Jason Jenkins
 * @version 0.3.0-20150727
 */
public class DoubleBorderSpan extends BorderSpan {

	/** Same as calling {@link #DoubleBorderSpan(int, int, int)} with 0 color.
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
	public DoubleBorderSpan( int start, int end) {
		super(start, end);
	}
	/**
	 * @param color The hex color to box with. If 0 the text colour is used.
	 * @param start The starting character to border
	 * @param end The ending character to border
	 */
	public DoubleBorderSpan(int color, int start, int end) {
		super(color, start, end);
	}

	@Override
	public void drawBackgroundCustom(Canvas canvas, Paint paint, int left,
			int right, int top, int baseline, int bottom, CharSequence text,
			int start, int end, int lnum) {
		int originalColor = paint.getColor();
        FontMetricsInt originalFontMetrics = paint.getFontMetricsInt();

        if (mColor != 0) {
        	paint.setColor( mColor );
        }
       
        //outer border
        int outerTop = baseline +  originalFontMetrics.ascent -  originalFontMetrics.descent;
        int innerTop = baseline +  originalFontMetrics.ascent -  originalFontMetrics.descent/2;
        
        int borderSpace = innerTop - outerTop;

        //inner border      
        int innerBottom = baseline +  originalFontMetrics.descent/2;
        int outterBottom =  innerBottom + borderSpace;
        
        right += borderSpace;
        drawBorder(canvas, paint, left, right, outerTop, outterBottom, text, start, end);
        
        
        right -= borderSpace;
        left += borderSpace;
        
        drawBorder(canvas, paint, left, right, innerTop, innerBottom, text, start, end);


        paint.setColor(originalColor);
	}
	
	
}
