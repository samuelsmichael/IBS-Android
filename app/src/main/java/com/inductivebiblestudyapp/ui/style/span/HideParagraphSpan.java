package com.inductivebiblestudyapp.ui.style.span;

import android.graphics.Paint.FontMetricsInt;
import android.text.style.LineHeightSpan;

/** Creates span that makes the text invisible via sizing. 
 * @version 0.1.2-20150909
 */
public class HideParagraphSpan extends HideSpan implements LineHeightSpan {

	@Override
	public void chooseHeight(CharSequence text, int start, int end,
			int spanstartv, int v, FontMetricsInt fm) {
		
		fm.ascent = 0;
		fm.top = 0;	
		fm.descent = 0;
		fm.bottom = 0;
	}
	
	
}
