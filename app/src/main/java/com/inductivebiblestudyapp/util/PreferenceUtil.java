package com.inductivebiblestudyapp.util;

import com.inductivebiblestudyapp.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 
 * @author Jason Jenkins
 * @version 0.1.0-20150618
 */
public class PreferenceUtil {

	public static SharedPreferences getPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	/** @return <code>true</code> if we have logged in before,
	 * <code>false</code> otherwise.
	 */
	public static boolean hasSignedIn(Context context) {
		return !getSigninType(context).isEmpty();
	}
	
	/**
	 * @param context 
	 * @return The login type or a blank string if unset.
	 */
	public static String getSigninType(Context context) {
		return getPreferences(context)
				.getString(context.getString(R.string.ibs_pref_KEY_SIGNIN_TYPE), "");
	}
	
	/**
	 * @param context 
	 * @return The user id or a blank string if unset.
	 */
	public static String getUserId(Context context) {
		return getPreferences(context)
				.getString(context.getString(R.string.ibs_pref_KEY_USER_SOCIAL_ID), "");
	}
}
