package com.inductivebiblestudyapp.data.loaders;

import android.content.Context;
import android.os.Bundle;

import com.inductivebiblestudyapp.data.RestClient;
import com.inductivebiblestudyapp.data.model.UpdateResult;
import com.inductivebiblestudyapp.data.service.NoteEditService;

/**
 * Loader to edit/create note. 
 * <code>true</code> on success, <code>false</code> or <code>null</code> on failure.
 * @author Jason Jenkins
 * @version 0.1.1-20150708
 */
public class NoteEditAsyncLoader extends AbstractUpdateAsyncLoader<NoteEditService> {
	final static private String CLASS_NAME = NoteEditAsyncLoader.class
			.getSimpleName();
	
	public static final String KEY_TEXT = CLASS_NAME + ".KEY_TEXT";
	public static final String KEY_VERSE_ID = CLASS_NAME + ".KEY_VERSE_ID";
	public static final String KEY_NOTE_ID = CLASS_NAME + ".KEY_NOTE_ID";

	
	private final String mText;
	private final String mId;
	private final boolean mEditMode;
	
	/**
	 * 
	 * @param context
	 * @param args
	 */
	public NoteEditAsyncLoader(Context context, Bundle args) {
		super(context, RestClient.getInstance().getNoteEditService());
		if (	!args.containsKey(KEY_TEXT) && 
				!(	args.containsKey(KEY_VERSE_ID) ||args.containsKey(KEY_NOTE_ID) )
				) {
			throw new IllegalArgumentException("Bundle is missing one or more minimum requirments: " + args);
		}		
		
		mText = args.getString(KEY_TEXT, "");
		mEditMode = args.containsKey(KEY_NOTE_ID);
		mId = args.getString(mEditMode ? KEY_NOTE_ID : KEY_VERSE_ID, "");
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
