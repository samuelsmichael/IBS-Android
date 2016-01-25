package com.inductivebiblestudyapp.ui.style.span;

import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;

/** Creates span that makes the text invisible via sizing. 
 * @version 0.1.2-20150909
 */
public class HideSpan extends RelativeSizeSpan {
	private static final float INVISIBLE_SIZE = 0.000000001f; //1 / billion
	public HideSpan() {
		super(INVISIBLE_SIZE);
	}
	
	@Override
	public void updateDrawState(TextPaint ds) {
		updateTextPaint(ds);
	}

	@Override
	public void updateMeasureState(TextPaint ds) {
		updateTextPaint(ds);
	}
	
	protected void updateTextPaint(TextPaint ds) {
    	ds.setTextSize(ds.getTextSize() * INVISIBLE_SIZE);
    	ds.setAlpha(0); //ensure the text is not visible
    }
}
