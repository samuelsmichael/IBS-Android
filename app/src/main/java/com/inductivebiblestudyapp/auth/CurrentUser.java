package com.inductivebiblestudyapp.auth;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.inductivebiblestudyapp.R;
import com.inductivebiblestudyapp.data.model.UserData;
import com.inductivebiblestudyapp.ui.RestartReceiver;
import com.inductivebiblestudyapp.util.PreferenceUtil;

/**
 * Helps manage the current user with IBS database. 
 * 
 * @author Jason Jenkins
 * @version 0.13.0-20150910
 */
public class CurrentUser {
	
	public static void updateUserData(Context context, UserData userData) {

		final SharedPreferences pref = PreferenceUtil.getPreferences(context);
		final SharedPreferences.Editor editor = pref.edit();
		final Resources res = context.getResources();
		final boolean userSetup = userData.hasUserSetup();
				
		editor.putString(res.getString(R.string.ibs_pref_KEY_IBS_ACCESS_TOKEN), userData.getAccessToken());
		
		//the server will tell use if the user has setup
		editor.putBoolean(res.getString(R.string.ibs_pref_KEY_USER_HAS_SETUP), userSetup);
		
		if (userSetup) { //we can overwrite all values, including with blanks,
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_MEMBER_ID), ""+userData.getMemberId());
			
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_PROFILE_IMAGE_URL), userData.getProfileImage());
			
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_FIRSTNAME), userData.getFirstName());
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_LASTNAME), userData.getLastName());			

			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_EMAIL), userData.getEmail());
			
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_ADDRESS), userData.getAddress());
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_CITY), userData.getCity());
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_STATE), userData.getState());
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_ZIP), userData.getZip());
			
			editor.putString(res.getString(R.string.ibs_pref_KEY_USER_BIO), userData.getBio());
			
			
			editor.putString(res.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID), userData.getBibleVersionId());
			editor.putString(res.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_NAME), userData.getBibleVersionName());
		} else {
			//we will only insert non-blank values.
			
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_PROFILE_IMAGE_URL), userData.getProfileImage());
			
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_FIRSTNAME), userData.getFirstName());
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_LASTNAME), userData.getLastName());			

			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_EMAIL), userData.getEmail());
			
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_ADDRESS), userData.getAddress());
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_CITY), userData.getCity());
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_STATE), userData.getState());
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_ZIP), userData.getZip());
			
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_USER_BIO), userData.getBio());
			
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID), userData.getBibleVersionId());
			putStringIfValue(editor, res.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_NAME), userData.getBibleVersionName());
		}
		
		editor.commit();
	}
	
	/** Clears all the user credentials & restarts the app (finishing the current activity).
	 * @return <code>true</code> if logout has been successful. */
	public static boolean logout(Activity activity) {
		final SharedPreferences pref = PreferenceUtil.getPreferences(activity);
		final SharedPreferences.Editor editor = pref.edit();
		
		//may be simpler to call editor.clear(), but this is more precise.
		String[] keysToEmpty = {
				activity.getString(R.string.ibs_pref_KEY_IBS_ACCESS_TOKEN),
				activity.getString(R.string.ibs_pref_KEY_USER_HAS_SETUP),
				
				activity.getString(R.string.ibs_pref_KEY_USER_PROFILE_IMAGE_URL),
				
				activity.getString(R.string.ibs_pref_KEY_USER_FIRSTNAME),
				activity.getString(R.string.ibs_pref_KEY_USER_LASTNAME),
				
				activity.getString(R.string.ibs_pref_KEY_USER_EMAIL),
				
				activity.getString(R.string.ibs_pref_KEY_USER_ADDRESS),
				activity.getString(R.string.ibs_pref_KEY_USER_CITY),
				activity.getString(R.string.ibs_pref_KEY_USER_STATE),
				activity.getString(R.string.ibs_pref_KEY_USER_ZIP),
				
				activity.getString(R.string.ibs_pref_KEY_USER_BIO),
				activity.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID),
				activity.getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_NAME),
				
				activity.getString(R.string.ibs_pref_KEY_SIGNIN_TYPE),
				activity.getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN),
				activity.getString(R.string.ibs_pref_KEY_SIGNIN_ACCESS_TOKEN_SECRET),
				activity.getString(R.string.ibs_pref_KEY_USER_SOCIAL_ID)				
		};
		
		for (String key : keysToEmpty) {
			editor.remove(key);
		}
		editor.commit();
		
		//response was not successful, thus clear preferences and return to top.
		// WARNING: Beware of loops here
		RestartReceiver.sendBroadcast(activity);
		return true;
	}
	
	public static void setLastUpdateNow(Context context) {
		final SharedPreferences pref = PreferenceUtil.getPreferences(context);
		final Resources res = context.getResources();
		
		pref.edit().putLong(res.getString(R.string.ibs_pref_KEY_PROFILE_UPDATE), new Date().getTime()).commit();
	}
	public static long getLastUpdateTime(Context context) {
		final SharedPreferences pref = PreferenceUtil.getPreferences(context);
		final Resources res = context.getResources();
		
		return pref.getLong(res.getString(R.string.ibs_pref_KEY_PROFILE_UPDATE), 0);
	}
	
	public static boolean hasAccessToken(Context context) {
		final SharedPreferences pref = PreferenceUtil.getPreferences(context);
		final Resources res = context.getResources();
		
		//if the first or last name are not empty.
		return ! pref.getString(res.getString(R.string.ibs_pref_KEY_IBS_ACCESS_TOKEN), "").isEmpty();
	}
	
	public static boolean hasCompletedSetup(Context context) {
		final SharedPreferences pref = PreferenceUtil.getPreferences(context);
		final Resources res = context.getResources();
		
		return pref.getBoolean(res.getString(R.string.ibs_pref_KEY_USER_HAS_SETUP), false);
	}
	
	public static void userHasCompletedSetup(Context context, boolean setup) {
		final SharedPreferences.Editor editor = PreferenceUtil.getPreferences(context).edit();
		final Resources res = context.getResources();
		editor.putBoolean(res.getString(R.string.ibs_pref_KEY_USER_HAS_SETUP), setup);
		editor.commit();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// End static methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private final SharedPreferences mPrefs;
	private final Resources mRes;
	private SharedPreferences.OnSharedPreferenceChangeListener mChangeListener = null;
	
	public CurrentUser(Context context) {
		mPrefs = PreferenceUtil.getPreferences(context);
		mRes = context.getResources();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Start public methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public void setIBSAccessToken(String accessToken) {
		mPrefs.edit()
			.putString(getString(R.string.ibs_pref_KEY_IBS_ACCESS_TOKEN), accessToken)
			.commit();
	}
	
	public void setProfileImage(String path) {
		putStringPref(R.string.ibs_pref_KEY_USER_PROFILE_IMAGE_URL, path);
	}
	
	public void setFirstName(String name) {
		putStringPref(R.string.ibs_pref_KEY_USER_FIRSTNAME, name);
	}
	
	public void setEmail(String email) {
		putStringPref(R.string.ibs_pref_KEY_USER_EMAIL, email);
	}
	
	public void setLastName(String name) {
		putStringPref(R.string.ibs_pref_KEY_USER_LASTNAME, name);
	}
	
	public void setAddress(String address) {
		putStringPref(R.string.ibs_pref_KEY_USER_ADDRESS, address);
	}
	
	public void setCity(String city) {
		putStringPref(R.string.ibs_pref_KEY_USER_CITY, city);
	}
	
	public void setState(String state) {
		putStringPref(R.string.ibs_pref_KEY_USER_STATE, state);
	}
	
	public void setZip(String zip) {
		putStringPref(R.string.ibs_pref_KEY_USER_ZIP, zip);
	}
	
	public void setBio(String bio) {
		putStringPref(R.string.ibs_pref_KEY_USER_BIO, bio);
	}
	
	public void setTranslationId(String id){
		putStringPref(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID, id);
	}
	
	public void setTranslationName(String name) {
		putStringPref(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_NAME, name);
	}
	
	/** @param listener The object interested on when the translation updates. */
	public void setOnTranslationUpdateListener(final OnTranslationUpdateListener listener) {
		mChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if(key.equals(getString(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID))){
					try {
						listener.onTranslationUpdate();
					} catch (Exception e) {
						Log.w(CurrentUser.class.getSimpleName(), "Error happened during translation change", e);
					}
				}				
			}
		};
		mPrefs.registerOnSharedPreferenceChangeListener(mChangeListener);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Accesssors
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks if the user is registered by email or social.
	 * @return <code>true</code> if email sign in, <code>false</code> if social signin.
	 */
	public boolean isEmailSignin() {
		return getStringPref(R.string.ibs_pref_KEY_SIGNIN_TYPE, "")
				.equals(getString(R.string.ibs_pref_VALUE_SIGNIN_EMAIL));
	}
	
	public String getMemberId() {
		return getStringPref(R.string.ibs_pref_KEY_USER_MEMBER_ID, "");
	}
	
	/** This is not to be confused with the sign in token. */
	public String getIBSAccessToken() {
		return getStringPref(R.string.ibs_pref_KEY_IBS_ACCESS_TOKEN, "");
	}
	
	public String getProfileImagePath() {
		return getStringPref(R.string.ibs_pref_KEY_USER_PROFILE_IMAGE_URL, "");
	}
	
	public String getFirstName() {
		return getStringPref(R.string.ibs_pref_KEY_USER_FIRSTNAME, "");
	}
	
	public String getLastName() {
		return getStringPref(R.string.ibs_pref_KEY_USER_LASTNAME, "");
	}
	
	public String getEmail() {
		return getStringPref(R.string.ibs_pref_KEY_USER_EMAIL, "");
	}
	
	public String getAddress() {
		return getStringPref(R.string.ibs_pref_KEY_USER_ADDRESS, "");
	}
	
	public String getCity() {
		return getStringPref(R.string.ibs_pref_KEY_USER_CITY, "");
	}
	
	public String getState() {
		return getStringPref(R.string.ibs_pref_KEY_USER_STATE, "");
	}
	
	public String getZip() {
		return getStringPref(R.string.ibs_pref_KEY_USER_ZIP, "");
	}
	
	public String getBio() {
		return getStringPref(R.string.ibs_pref_KEY_USER_BIO, "");
	}
	
	public String getTranslationId(){
		return getStringPref(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_ID, "");
	}
	
	public String getTranslationName() {
		return getStringPref(R.string.ibs_pref_KEY_BIBLE_TRANSLATION_NAME, "");
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	private static boolean putStringIfValue(SharedPreferences.Editor editor, String key, String value) {
		if (value != null && !value.isEmpty()) {
			editor.putString(key, value);
			return true;
		}
		return false;
	}
	
	private void putStringPref(int id, String value) {
		if (value == null) {
			return; //we cannot set null
		}
		mPrefs.edit().putString(getString(id), value.trim()).commit();
	}
	
	private String getStringPref(int id, String defaultValue) {
		return mPrefs.getString(getString(id), defaultValue).trim();
	}
	
	private String getString(int id) {
		return mRes.getString(id);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//// Public interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** An interface for fragments interested in knowing when to update their contents
	 * based on bible version/translation.
	 * 
	 * @author Jason Jenkins
	 * @version 0.1.0-20150717
	 */
	public interface OnTranslationUpdateListener {
		/**
		 * Work to be done after the translation has changed to be done here.
		 * @return <code>false</code> when the listener does nothing,
		 * <code>true</code> otherwise.
		 */
		public void onTranslationUpdate();
	}

}
