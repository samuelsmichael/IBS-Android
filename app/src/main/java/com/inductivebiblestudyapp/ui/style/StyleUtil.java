package com.inductivebiblestudyapp.ui.style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

/** @version 0.1.0-20150824 */
public class StyleUtil {

	/**
	 * Finds word and sets each word to bold and 1.1 the orignal size.
	 * @param word The word to search for
	 * @param spanString The spannable string to style
	 * @param spanFlag see {@link Spannable} for available flags
	 * @return <code>true</code> if found, <code>false</code> if not. */
	public static boolean findWordAndBold(final String word, SpannableString spanString, int spanFlag) {
		return findWordAndStyle(word, spanString, spanFlag, true, false);
	}
	
	/**
	 * Finds word and sets each word to bold and 1.1 the orignal size.
	 * @param word The word to search for
	 * @param spanString The spannable string to style
	 * @param spanFlag see {@link Spannable} for available flags
	 * @return <code>true</code> if found, <code>false</code> if not. */
	public static boolean findWordAndBoldUnderline(final String word, SpannableString spanString, int spanFlag) {
		return findWordAndStyle(word, spanString, spanFlag, true, true);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Util methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static boolean findWordAndStyle(final String word, SpannableString spanString, int spanFlag, boolean bold, boolean underline) {
		boolean found = false;
		Matcher matcher = Pattern.compile("(?<=\\b)" + word + "(?=\\b)").matcher(spanString);
		
		while (matcher.find()) {
			found = true;
			final int start = matcher.start();
			final int end = matcher.end();
			if (bold) {
				spanString.setSpan(new StyleSpan(Typeface.BOLD), start, end, spanFlag);
				spanString.setSpan(new RelativeSizeSpan(1.1f), start, end, spanFlag);
			}
			if (underline) {
				spanString.setSpan(new UnderlineSpan(), start, end, spanFlag);
			}
		}
		return found;
	} 
}
