package com.inductivebiblestudyapp.ui.style;

import com.inductivebiblestudyapp.ui.style.span.AbstractCustomBackgroundSpan;
import com.inductivebiblestudyapp.ui.style.span.BorderSpan;
import com.inductivebiblestudyapp.ui.style.span.DoubleBorderSpan;
import com.inductivebiblestudyapp.ui.style.span.DoubleUnderlineSpan;
import com.inductivebiblestudyapp.ui.style.span.ObjectEnabledCallback;
import com.inductivebiblestudyapp.ui.style.span.PaintBackgroundColorSpan;
import com.inductivebiblestudyapp.ui.style.span.AbstractCustomBackgroundSpan.OnMeasureCallback;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

/**
 * Text style container that helps aggregate and apply styles in a Parcelable form.
 * 
 * @author Jason Jenkins
 * @version 0.7.0-20150803
 */
public class TextStyle implements Parcelable {

	private static int DEFAULT_SPAN_FLAG = 0;
	
	/**
	 * Builds a TextStyle object.
	 * @author Jason Jenkins
	 * @version 0.1.0-20150616
	 */
	public static class Builder {
		private TextStyle style = new TextStyle();
		
		public Builder set(TextStyle style) {
			this.style = style;
			return this;
		}
		
		public Builder setBold(boolean enabled) {
			style.mBold = enabled;
			return this;
		}
		
		public Builder setItalics(boolean enabled) {
			style.mItalics = enabled;
			return this;
		}
		
		public Builder setUnderline(boolean enabled) {
			style.mUnderline = enabled;
			return this;
		}
		
		public Builder setStrikeThrough(boolean enabled) {
			style.mStrikeThrough = enabled;
			return this;
		}
		
		public Builder setBoxed(boolean enabled) {
			style.mBoxed = enabled;
			return this;
		}
		
		public Builder setDoubleUnderline(boolean enabled) {
			style.mDoubleUnderline = enabled;
			return this;
		}
		
		public Builder setDoubleBoxed(boolean enabled) {
			style.mDoubleBoxed = enabled;
			return this;
		}
		
		/**
		 * @param color set to 0 to unset.
		 */
		public Builder setTextColor(int color) {
			style.mTextColor = color;
			return this;
		}
		
		/**
		 * @param color set to 0 to unset.
		 */
		public Builder setHighlightColor(int color) {
			style.mHighlightColor = color;
			return this;
		}

		
		public TextStyle build() {
			return style;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End builder
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The default flag used in {@link SpannableString#setSpan(Object, int, int, int)}  when creating objects */
	public static void setDefaultSpanFlag(int flag) {
		TextStyle.DEFAULT_SPAN_FLAG = flag;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End statics
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private ObjectEnabledCallback mEnableCallback = null;
	
	private OnMeasureCallback mMeasureCallback = null;
	
	private boolean mCentered = false;
	
	
	private int spanFlag = DEFAULT_SPAN_FLAG;
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End custom span extras
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private boolean mBold = false;
	private boolean mItalics = false;
	private boolean mUnderline = false;
	private boolean mStrikeThrough = false;
	
	private boolean mDoubleUnderline = false;
	private boolean mBoxed = false;
	private boolean mDoubleBoxed = false;
	
	private int mHighlightColor = 0; //that is black alpha
	private int mTextColor = 0;
	
	private TextStyle(){}
	
	public TextStyle(Parcel src) {
		boolean[] bools = new boolean[7];
		int[] ints = new int[2];
		
		src.readBooleanArray(bools);
		src.readIntArray(ints);
		
		mBold = bools[0];
		mItalics = bools[1];
		mUnderline = bools[2];
		mStrikeThrough = bools[3];
		
		mDoubleUnderline = bools[4];
		mBoxed = bools[5];
		mDoubleBoxed = bools[6];
		
		mHighlightColor = ints[0];
		mTextColor = ints[1];
		
	}	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		boolean[] bools = new boolean[]{
			mBold,
			mItalics,
			mUnderline,
			mStrikeThrough,
			
			mDoubleUnderline,
			mBoxed,
			mDoubleBoxed
		};
		
		int[] ints = new int[]{
			mHighlightColor,
			mTextColor
		};
		
		dest.writeBooleanArray(bools);
		dest.writeIntArray(ints);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The flag used in {@link SpannableString#setSpan(Object, int, int, int)} 
	 * for this object. */
	public TextStyle setSpanFlag(int flag) {
		this.spanFlag = flag;
		return this;
	}
	
	/**
	 * Required for centered texts & boxed, double-boxed, double-underline & background
	 * styles to work.
	 * @param mCentered
	 */
	public TextStyle setCentered(boolean mCentered) {
		this.mCentered = mCentered;
		return this;
	}
		

	
	/**
	 * Required for centered texts & boxed, double-boxed, double-underline & background
	 * styles to measure correctly with other styles.
	 * */
	public TextStyle setOnMeasureCallback(OnMeasureCallback callback) {
		this.mMeasureCallback = callback;
		return this;
	}
	
	/**
	 * Required for centered texts & boxed, double-boxed, double-underline & background
	 * styles to work.
	 * */
	public TextStyle setEnabedCallback(ObjectEnabledCallback callback) {
		this.mEnableCallback = callback;
		return this;
	}
	
	
	
	/**
	 * Applies all relevant styles previous set by the builder.
	 * @param text The text to apply styles to
	 * @param start The first character to apply the style to
	 * @param end The last character + 1 to apply style to
	 * @return The text styled within the given range.
	 * 
	 * @throws IndexOutOfBoundsException If the start & end do not make sense with the text,
	 * for example: negative values, start after end, end longer than text, etc.
	 */
	public SpannableString apply(final String text, final int start, final int end) {
		return apply(new SpannableString(text), start, end);
	}
	
	/**
	 * Applies all relevant styles previous set by the builder.
	 * @param spanString The spannable string to apply styles to
	 * @param start The first character to apply the style to
	 * @param end The last character + 1 to apply style to
	 * @return The text styled within the given range.
	 * 
	 * @throws IndexOutOfBoundsException If the start & end do not make sense with the text,
	 * for example: negative values, start after end, end longer than text, etc.
	 */
	public SpannableString apply(final SpannableString spanString, final int start, final int end) {
		return apply(spanString, start, end, 0, spanString.length(), 0);		
	}
	/**
	 * Applies all relevant styles previous set by the builder.
	 * @param spanString The spannable string to apply styles to
	 * @param start The first character to apply the style to
	 * @param end The last character + 1 to apply style to
	 * @param paragraphStart The start of the paragraph (required for box, double box, double underline & background to work. )
	 * @param paragraphEnd The end of the paragraph (required for box, double box, double underline & background to work. )
	 * @param offset The offset to give to the start + end of the custom styles.
	 * @return The text styled within the given range.
	 * 
	 * @throws IndexOutOfBoundsException If the start & end do not make sense with the text,
	 * for example: negative values, start after end, end longer than text, etc.
	 */
	public SpannableString apply(final SpannableString spanString, final int start, final int end, 
			final int paragraphStart, final int paragraphEnd, final int offset) {
		final int FLAGS = spanFlag;
		
		//sanity checks
		if (start > end) {
			throw new IndexOutOfBoundsException("Start cannot be larger than end: " + 
					start + " > " + end);
		} else if (start < 0 || end < 0) {
			throw new IndexOutOfBoundsException("Indices cannot be negative");
		} else if (end > spanString.length()) {
			throw new IndexOutOfBoundsException("Indices cannot be larger than input text: " + 
					end + " > " + spanString.length()); 
		}		
		
		if (mBold && mItalics) {
			spanString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, FLAGS);
		} else if (mBold) {
			spanString.setSpan(new StyleSpan(Typeface.BOLD), start, end, FLAGS);
		} else if (mItalics) {
			spanString.setSpan(new StyleSpan(Typeface.ITALIC), start, end, FLAGS);
		}
		
		if (mUnderline) {
			spanString.setSpan(new UnderlineSpan(), start, end, FLAGS);
		}
		
		if (mStrikeThrough) {
			spanString.setSpan(new StrikethroughSpan(), start, end, FLAGS);
		}
		
		if (mTextColor != 0) {
			spanString.setSpan(new ForegroundColorSpan(mTextColor), start, end, FLAGS);
		}
		
		///////////////////////////////////////////////////////////////////////////
		//// End of easy styles
		///////////////////////////////////////////////////////////////////////////
		
		//Background MUST come first
		if (mHighlightColor != 0) {
			// Note: We require this for compatibility with the 
			// box, double box, & double underline spans
			PaintBackgroundColorSpan span = 
					new PaintBackgroundColorSpan(mHighlightColor, start + offset, end + offset);
			setArgsAndSpan(span, spanString, paragraphStart, paragraphEnd, FLAGS);
		}
		
		if (mBoxed) {
			BorderSpan span = 
					new BorderSpan(mTextColor, start + offset, end  + offset);
			setArgsAndSpan(span, spanString, paragraphStart, paragraphEnd, FLAGS);
		}
		
		if (mDoubleBoxed) {
			DoubleBorderSpan span = 
					new DoubleBorderSpan(mTextColor, start + offset, end + offset);
			setArgsAndSpan(span, spanString, paragraphStart, paragraphEnd, FLAGS);
		}
		
		if (mDoubleUnderline) {
			DoubleUnderlineSpan span = 
					new DoubleUnderlineSpan(mTextColor, start + offset, end + offset);
			setArgsAndSpan(span, spanString, paragraphStart, paragraphEnd, FLAGS);
		}
		
		return spanString;
	}

	/** Set arts for the spans. */
	private void setArgsAndSpan(AbstractCustomBackgroundSpan span, final SpannableString spanString, 
			final int paragraphStart, int paragraphEnd, final int FLAGS) {
		span.setCentered(mCentered);
		span.setEnabledCallback(mEnableCallback);
		span.setOnMeasureCallback(mMeasureCallback);
		spanString.setSpan(span, paragraphStart, paragraphEnd, FLAGS);
	}

}
