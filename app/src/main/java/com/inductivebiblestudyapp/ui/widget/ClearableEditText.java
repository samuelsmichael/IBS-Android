/**
 * Copyright 2015 Alex Yanchenko
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
package com.inductivebiblestudyapp.ui.widget;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.EditText;

/**
 * Based on: 
 * https://github.com/yanchenko/droidparts/blob/develop/droidparts/src/org/droidparts/widget/ClearableEditText.java
 * 
 * <br/>
 * To change clear icon, set
 * 
 * <pre>
 * android:drawableRight="@drawable/custom_icon"
 * </pre>
 * May or may not cause issues with {@link #setError(CharSequence)} & {@link #setError(CharSequence, Drawable)}.
 * @version 0.2.0-20150807
 */
public class ClearableEditText extends AppCompatEditText implements OnTouchListener, OnFocusChangeListener  {

	private Drawable mClearDrawable;
	private OnClearTextListener mClearTextListener;
	

	private OnTouchListener mTouchListener;
	private OnFocusChangeListener mFocusListener;

	//private float mStartX = 0;
	//private float mStartY = 0;
	
	private float mTouchSlop;

	public ClearableEditText(Context context) {
		super(context);
		init();
	}

	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setOnClearTextListener(OnClearTextListener listener) {
		this.mClearTextListener = listener;
	}


	@Override
	public void setOnTouchListener(OnTouchListener l) {
		this.mTouchListener = l;
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener f) {
		this.mFocusListener = f;
	}

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			final float rightBoxWidth = getWidth() - getPaddingRight() - mClearDrawable.getIntrinsicWidth();
			
			if (event.getX() > rightBoxWidth) { //if it has been touched
				switch (event.getAction()) {
					
				case MotionEvent.ACTION_UP:
					//TODO proper click detection
					setText("");
					if (mClearTextListener != null) {
						mClearTextListener.onClearText();
					}
					break;
				}				
				return true;
			}
		}
		if (mTouchListener != null) {
			return mTouchListener.onTouch(v, event);
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(isNotEmpty(getText()));
		} else {
			setClearIconVisible(false);
		}
		if (mFocusListener != null) {
			mFocusListener.onFocusChange(v, hasFocus);
		}
	}
	
	@Override
	public void setError(CharSequence error, Drawable icon) {
		super.setError(error, icon);
		if (error == null) {
			setClearIconVisible(isNotEmpty(getText()));
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	private void init() {
		mClearDrawable = getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			mClearDrawable = getResources().getDrawable(android.R.drawable.presence_offline);
		}
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
		setClearIconVisible(false);
		super.setOnTouchListener(this);
		super.setOnFocusChangeListener(this);
		addTextChangedListener(new TextWatcherAdapter(this, mTextWatcherListener));
		
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	protected void setClearIconVisible(boolean visible) {
		boolean wasVisible = (getCompoundDrawables()[2] != null);
		if (visible != wasVisible) {
			Drawable x = visible ? mClearDrawable : null;
			setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static boolean isClick(float startX, float endX, float startY, float endY, float touchSlop) {
		float xDiff = Math.abs(startX - endX);
		float yDiff = Math.abs(startY - endY);
		return (xDiff > touchSlop || yDiff > touchSlop);
	} 
	

	/*package*/ static boolean isNotEmpty(CharSequence text) {
		return text != null && text.length() > 0;
	}
	
	
	
	private TextWatcherListener mTextWatcherListener = new TextWatcherListener() {
		
		@Override
		public void onTextChanged(EditText view, String text) {
			if (isFocused()) {
				setClearIconVisible(isNotEmpty(text));
			}
		}
	};
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	public interface OnClearTextListener {
		void onClearText();
	}

	
	/*package*/ static interface TextWatcherListener {

		void onTextChanged(EditText view, String text);

	}
	
	/*package*/ static class TextWatcherAdapter implements TextWatcher {		

		private final EditText mView;
		private final TextWatcherListener mListener;

		public TextWatcherAdapter(EditText editText, TextWatcherListener listener) {
			this.mView = editText;
			this.mListener = listener;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mListener.onTextChanged(mView, s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// pass
		}

		@Override
		public void afterTextChanged(Editable s) {
			// pass
		}

	} 
}