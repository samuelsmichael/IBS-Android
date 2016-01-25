package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.os.Bundle;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.ChapterThemeEditService;

/**
 * Loader to edit/create note. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.1.1-20150708
 */
public class ChapterThemeEditAsyncLoader extends AbstractUpdateAsyncLoader<ChapterThemeEditService> {
	final static private String CLASS_NAME = ChapterThemeEditAsyncLoader.class
			.getSimpleName();
	
	public static final String KEY_TEXT = CLASS_NAME + ".KEY_TEXT";
	public static final String KEY_CHAPTER_ID = CLASS_NAME + ".KEY_CHAPTER_ID";
	public static final String KEY_THEME_ID = CLASS_NAME + ".KEY_THEME_ID";

	
	private final String mText;
	private final String mId;
	private final boolean mEditMode;
	
	/**
	 * 
	 * @param context
	 * @param args
	 */
	public ChapterThemeEditAsyncLoader(Context context, Bundle args) {
		super(context, RestClient.getInstance().getChapterThemeEditService());
		if (	!args.containsKey(KEY_TEXT) && 
				!(	args.containsKey(KEY_CHAPTER_ID) ||args.containsKey(KEY_THEME_ID) )
				) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}		
		
		mText = args.getString(KEY_TEXT, "");
		mEditMode = args.containsKey(KEY_THEME_ID);
		mId = args.getString(mEditMode ? KEY_THEME_ID : KEY_CHAPTER_ID, "");
	}
	
	@Override
	protected Boolean fetchResult() {
		UpdateResult result = null;
		
		if (mEditMode) {
			result = mService.edit(mAccessToken, mId, mText);
		} else {
			result = mService.create(mAccessToken, mId, mText);
		}
		
		if (result != null) {
			return result.isSuccessful();
		}
		return false;
	}	
}
