package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.ProfileUpdateService;
import com.inductivebiblestudyapp.util.ImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Loader to update profile api using {@link AsyncTaskLoader}. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.5.2-20150915
 */
public class ProfileUpdateAsyncLoader extends AbstractUpdateAsyncLoader2<ProfileUpdateService> {
	final static private String CLASS_NAME = ProfileUpdateAsyncLoader.class
			.getSimpleName();
	
	
	public static final String KEY_FIRSTNAME = CLASS_NAME + ".KEY_FIRSTNAME";
	public static final String KEY_LASTNAME = CLASS_NAME + ".KEY_LASTNAME";
	public static final String KEY_ADDRESS = CLASS_NAME + ".KEY_ADDRESS";
	public static final String KEY_STATE = CLASS_NAME + ".KEY_STATE";
	public static final String KEY_CITY = CLASS_NAME + ".KEY_CITY";
	public static final String KEY_ZIP = CLASS_NAME + ".KEY_ZIP";
	public static final String KEY_TRANSLATIONS = CLASS_NAME + ".KEY_TRANSLATIONS";
	
	/** Optional. */
	public static final String KEY_USER_BIO = CLASS_NAME + ".KEY_USER_BIO";
	/** Optional. */
	public static final String KEY_EMAIL = CLASS_NAME + ".KEY_EMAIL";
	/** Optional. */
	public static final String KEY_PASSWORD = CLASS_NAME + ".KEY_PASSWORD";
	
	private final Bundle mArgs;	
	private String mImagePath;
	
	public ProfileUpdateAsyncLoader(Context context, Bundle args) {
		super(context, RestClient.getInstance().getProfileUpdateService());
		if ( !(	args.containsKey(KEY_FIRSTNAME) && args.containsKey(KEY_LASTNAME) &&
				args.containsKey(KEY_TRANSLATIONS) )
				) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}
		this.mArgs = args;	
		mImagePath = "";
		
		if (args.getString(KEY_PASSWORD, "").isEmpty()) {
			args.remove(KEY_PASSWORD); //do not allow empty passwords
		}
	}
	
	public ProfileUpdateAsyncLoader(Context context, Bundle args, String imagePath) {
		this(context, args);
		if (imagePath.contains("file://")) {
			mImagePath = imagePath;
		} else {
			//do not bother to re-upload image
			mImagePath = "";
		}
	}
	
	

	@Override
	protected UpdateResult fetchResult() {
		UpdateResult result = null;
		Bitmap mBitmap = null;
		if (!TextUtils.isEmpty(mImagePath)) {
			mBitmap = ImageLoader.getInstance().loadImageSync(mImagePath);	
		}
		
		if (mArgs.containsKey(KEY_USER_BIO) && mBitmap != null) {
			String base64 = ImageUtil.bitmapToBase64(mBitmap);
			result = mService.sendProfileUpdate(
					mAccessToken, 
					mArgs.getString(KEY_FIRSTNAME), 
					mArgs.getString(KEY_LASTNAME),
					mArgs.getString(KEY_EMAIL, null),  //null if undefined, which means skip
					mArgs.getString(KEY_ADDRESS, null),
					mArgs.getString(KEY_CITY, null), 
					mArgs.getString(KEY_STATE, null), 
					mArgs.getString(KEY_ZIP, null), 
					mArgs.getString(KEY_TRANSLATIONS),
					mArgs.getString(KEY_USER_BIO, null),
					base64,
					mArgs.getString(KEY_PASSWORD, null));

		} else {
			result = mService.sendProfileUpdate(
					mAccessToken, 
					mArgs.getString(KEY_FIRSTNAME), 
					mArgs.getString(KEY_LASTNAME), 
					mArgs.getString(KEY_EMAIL, null),
					mArgs.getString(KEY_ADDRESS, null),
					mArgs.getString(KEY_CITY, null), 
					mArgs.getString(KEY_STATE, null), 
					mArgs.getString(KEY_ZIP, null), 
					mArgs.getString(KEY_TRANSLATIONS),
					mArgs.getString(KEY_USER_BIO, null),
					null,
					mArgs.getString(KEY_PASSWORD, null));
		}
		
		return result;
	}	
}
